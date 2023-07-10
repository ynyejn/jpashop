package jpabook.jpashop.service;

import jpabook.jpashop.domain.Account;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
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
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockService {
    private final OrderJpaRepository orderRepository;
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
                .usedStock(usedCount("stock:"+cs.getAccount().getAccountCode()+cs.getItem().getProductCode()))
                .finishedStock(usedCount("finish:"+cs.getAccount().getAccountCode()+cs.getItem().getProductCode()))
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
            List<DistributionRate> distributionRateList = distributionRateRepository.findAllByOrderBySortAsc();
            for (SapStock sapStock : sapStocks) {//제품별 총재고
                Long allStock = sapStock.getStock();
                Long availableStock = allStock;
                Long allStockRemainder = allStock % distributionRateList.size();
                Long remainderStock = allStockRemainder;
                Item product = sapStock.getItem();

                //홀딩재고 계산 (오더테이블에 미할당+상품명조건으로 검색) => 이때 사용중으로 들어감
                List<Order> unfixOrder = orderRepository.findAllByItemAndStatus(product, OrderStatus.ORDER);
                System.out.println(unfixOrder.size());
                for (Order order : unfixOrder) {
                    int count = order.getOrderItems().get(0).getCount();
                    if(availableStock-count>=0){
                        for (int i = 0 ; i< count ; i++){
                            order.setStatus(OrderStatus.ORDER_FIX);
                            String accountCode = order.getAccount().getAccountCode();
                            String productCode = order.getOrderItems().get(0).getItem().getProductCode();
                            String redisKey = "stock:"+accountCode+productCode;

                            //redis 추가
                            addOnRedis(redisKey, String.valueOf(order.getId())+(i+1));
                        }
                    }
                }


                //사용중재고 total 계산
                ScanOptions scanOptions = ScanOptions.scanOptions().match("stock*"+product.getProductCode()).count(500).build();
                Cursor<byte[]> keys = redisTemplate.getConnectionFactory().getConnection().scan(scanOptions);
                Long allUsedCount=0L;
                while (keys.hasNext()){
                    String key = new String(keys.next());
                    allUsedCount+=usedCount(key);
                }

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
                    double rate = distributionRate.getRate() / (double)100;
                    Long stock = Math.round((allStock-allStockRemainder-allUsedCount) * rate) + usedCount("stock:"+account.getAccountCode()+product.getProductCode());
                    if(remainderStock>0){
                        stock++;
                        remainderStock--;
                    }
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
            //완료재고삭제
            //사용중재고 total 계산
            ScanOptions scanOptions = ScanOptions.scanOptions().match("finish*").count(500).build();
            Cursor<byte[]> keys = redisTemplate.getConnectionFactory().getConnection().scan(scanOptions);
            while (keys.hasNext()){
                String key = new String(keys.next());
                delOnRedis(key);
            }

            return ResultResDataDto.fromResMsg(true, "성공");

        } catch (Exception e) {
            return ResultResDataDto.fromResMsg(false, "에러");

        }


    }



    @Transactional
    public ResultResDataDto redistributeStock() {   //사용안함
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
                if(avail-count>=0){
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
        Long usedStock = usedCount(redisKey);
        return allStock-usedStock;
    }

    public Long usedCount(String key){
        return redisTemplate.opsForSet().size(key);
    }
    public void addOnRedis(String key,String value){
        Long addSiz = redisTemplate.opsForSet().add(key, value);
    }
    private void delOnRedis(String key) {
        Boolean delete = redisTemplate.delete(key);
    }
    public void delOnRedis(String key,String value){
        Long remove = redisTemplate.opsForSet().remove(key,value);
    }
    private String generateKey(Long key){
        return key.toString();
    }

    public ResultResDataDto finishStock(Order order) {  //레디스 사용중인거에서 빼고 완료에 넣어야됨
        try {
            int count = order.getOrderItems().get(0).getCount();
            String accountCode = order.getAccount().getAccountCode();
            String productCode = order.getOrderItems().get(0).getItem().getProductCode();
            String redisFinishKey = "finish:"+accountCode+productCode;
            String redisKey = "stock:"+accountCode+productCode;
            delOnRedis(redisKey,String.valueOf(order.getId())+1);
            addOnRedis(redisFinishKey, String.valueOf(order.getId())+1);

            return ResultResDataDto.fromResMsg(true, "성공");
        }catch (Exception e){
            return ResultResDataDto.fromResMsg(false, "실패");
        }
    }
}
