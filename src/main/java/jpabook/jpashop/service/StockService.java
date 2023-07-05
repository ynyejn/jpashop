package jpabook.jpashop.service;

import jpabook.jpashop.domain.Account;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.stock.ChannelStock;
import jpabook.jpashop.domain.stock.ChannelStockList;
import jpabook.jpashop.domain.stock.DistributionRate;
import jpabook.jpashop.domain.stock.SapStock;
import jpabook.jpashop.dto.ChannelStockListDto;
import jpabook.jpashop.dto.ProductStockDto;
import jpabook.jpashop.dto.ResultResDataDto;
import jpabook.jpashop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    private final StockListRepository stockListRepository;
    private final SapStockRepository sapStockRepository;
    private final DistributionRateRepository distributionRateRepository;
    private final ItemService itemService;
    private final AccountService accountService;
    private final RedisTemplate<String,String> redisTemplate;


    public List<ChannelStockListDto> findStocks() {
        return stockListRepository.findAll().stream().map(cs->ChannelStockListDto
                .builder()
                .id(cs.getId())
                .accountName(cs.getAccount().getName())
                .productName(cs.getItem().getName())
                .stock(cs.getQty())
                .usedStock(totalUsedCount("stock:"+cs.getAccount().getAccountCode()+cs.getItem().getProductCode()))
                .status(cs.getChannelStock().getStatus())
                .build()).collect(Collectors.toList());
    }

    public int getSapStock() {
        Integer allStock = sapStockRepository.findSapStock();
        return allStock == null ? 0 : sapStockRepository.findSapStock();
    }

    @Transactional
    public ResultResDataDto distributeStock() {
        try {
            List<SapStock> sapStocks = sapStockRepository.findAllByDistributionFlag(0);
            List<DistributionRate> distributionRateList = distributionRateRepository.findAll();
            for (SapStock sapStock : sapStocks) {//제품별 총재고
                Long allStock = sapStock.getStock();
                Item product = sapStock.getItem();

                //channelStock
                ChannelStock channelStock = new ChannelStock();
                channelStock.setTotalQty(allStock);
                channelStock.setItem(product);
                channelStock.setStatus("가용재고");
                stockRepository.save(channelStock);

                //channelStockList
                for (DistributionRate distributionRate : distributionRateList) {

                    ChannelStockList channelStockList = null;
                    Account account = distributionRate.getAccount();
                    Long stock = allStock * distributionRate.getRate() / 100;

                    Optional<ChannelStockList> channelStockListOptional = stockListRepository.findByAccountAndItem(account,product);
                    if(channelStockListOptional.isEmpty()){ //기존값 없을경우
                        channelStockList = new ChannelStockList();
                        channelStockList.setChannelStock(channelStock);
                        channelStockList.setSapStock(sapStock);
                        channelStockList.setQty(stock);
                        channelStockList.setAccount(account);
                        channelStockList.setItem(product);
                    }else{
                        channelStockList = channelStockListOptional.get();
                        channelStockList.setChannelStock(channelStock);
                        channelStockList.setSapStock(sapStock);
                        channelStockList.setQty(stock);
                    }
                    stockListRepository.save(channelStockList);
                }
                sapStock.setDistributionFlag(1);
            }

            return ResultResDataDto.fromResMsg(true, "성공");

        } catch (Exception e) {
            return ResultResDataDto.fromResMsg(false, "에러");

        }


    }
    @Transactional
    public ResultResDataDto redistributeStock() {
        try {
            List<SapStock> sapStocks = sapStockRepository.findAllByDistributionFlag(0);
            List<DistributionRate> distributionRateList = distributionRateRepository.findAll();

            if (sapStocks.isEmpty()) {
                List<ProductStockDto> stockDtos = stockListRepository.findAllStockGroupByProduct()
                        .stream().map(s->ProductStockDto.builder()
                                .productCode(s.getProductCode())
                                .stock(s.getStock()).build()).collect(Collectors.toList());
                for (ProductStockDto stockDto : stockDtos) {
                    int allStock = stockDto.getStock();
                    Item product = itemService.findByProductCode(stockDto.getProductCode());
                    for (DistributionRate distributionRate : distributionRateList) {
                        Account account = distributionRate.getAccount();
                        int stock = allStock * distributionRate.getRate() / 100;
//                        ChannelStock channelStock = stockRepository.findByItemAndAccountAndStatus(product,account,"가용재고");
//                        channelStock.setStock(stock);
                    }
                }

            }

            return ResultResDataDto.fromResMsg(true, "성공");

        } catch (Exception e) {
            return ResultResDataDto.fromResMsg(false, "에러");

        }
    }

    public boolean checkStock(String accountCode, String productCode, int count) {
        Optional<ChannelStockList> channelStock = stockListRepository.checkStock(accountCode, productCode, count);
        if (channelStock.isEmpty()) {
            return false;
        } else {
//            ChannelStock channelStock1 = channelStock.get();
//            int allStock  = channelStock1.getStock();
            return true;
        }
    }

    @Transactional
    public ResultResDataDto fixStock(Order order) {
        try {
            int count = order.getOrderItems().get(0).getCount();
            String accountCode = order.getAccount().getAccountCode();
            String productCode = order.getOrderItems().get(0).getItem().getProductCode();
            //findBy해서 account 랑 item으로 하는것으로 수정하자
            Optional<ChannelStockList> checkStock = stockListRepository.checkStock(accountCode, productCode, count);
            if (!checkStock.isEmpty()) {
                //--------------조회시 다른 트랙잭션에서 조회 불가하도록 lock 시작--------------
                ChannelStockList channelStockList = stockListRepository.findPessimisticById(checkStock.get().getId());
                String redisKey = "stock:"+accountCode+productCode;
                Long avail = availableStock(channelStockList.getId());
                System.out.println(avail);
                if(avail-count>0){
                    for (int i = 0 ; i< count ; i++){
                        //redis 추가
                        addOnRedis(redisKey, String.valueOf(order.getId())+(i+1));
                    }
                }else{
                    return ResultResDataDto.fromResMsg(false, "재고가 없어요");
                }
            } else {
                return ResultResDataDto.fromResMsg(false, "재고가 없어요");
            }
            return ResultResDataDto.fromResMsg(true, "성공");

        } catch (Exception e) {
            return ResultResDataDto.fromResMsg(false, "실패");

        }
    }
    public Long availableStock(Long key){
        ChannelStockList channelStockList = stockListRepository.findById(key).get();
        Long allStock = channelStockList.getQty();
        String redisKey = "stock:"+channelStockList.getAccount().getAccountCode()+channelStockList.getItem().getProductCode();
        Long usedStock = totalUsedCount(redisKey);
        return allStock-usedStock;
    }

    public Long totalUsedCount(String key){
        return redisTemplate.opsForSet().size(key);
    }
    public void addOnRedis(String key,String value){
        Long addSiz = redisTemplate.opsForSet().add(key, value);
    }
    private String generateKey(Long key){
        return key.toString();
    }

}
