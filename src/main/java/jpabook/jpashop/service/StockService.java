package jpabook.jpashop.service;

import jpabook.jpashop.domain.Account;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.stock.ChannelStock;
import jpabook.jpashop.domain.stock.DistributionRate;
import jpabook.jpashop.domain.stock.SapStock;
import jpabook.jpashop.dto.ProductStockDto;
import jpabook.jpashop.dto.ResultResDataDto;
import jpabook.jpashop.repository.DistributionRateRepository;
import jpabook.jpashop.repository.SapStockRepository;
import jpabook.jpashop.repository.StockRepository;
import lombok.RequiredArgsConstructor;
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
    private final SapStockRepository sapStockRepository;
    private final DistributionRateRepository distributionRateRepository;
    private final ItemService itemService;

    public List<ChannelStock> findStocks() {
        return stockRepository.findAll();
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
            for (SapStock sapStock : sapStocks) {
                for (DistributionRate distributionRate : distributionRateList) {
                    int allStock = sapStock.getStock();
                    Item product = sapStock.getItem();
                    Account account = distributionRate.getAccount();
                    int stock = allStock * distributionRate.getRate() / 100;
                    ChannelStock channelStock = new ChannelStock();
                    channelStock.setStock(stock);
                    channelStock.setItem(product);
                    channelStock.setAccount(account);
                    channelStock.setStatus("가용재고");
                    stockRepository.save(channelStock);
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
                List<ProductStockDto> stockDtos = stockRepository.findAllStockGroupByProduct()
                        .stream().map(s->ProductStockDto.builder()
                                .productCode(s.getProductCode())
                                .stock(s.getStock()).build()).collect(Collectors.toList());
                for (ProductStockDto stockDto : stockDtos) {
                    int allStock = stockDto.getStock();
                    Item product = itemService.findByProductCode(stockDto.getProductCode());
                    for (DistributionRate distributionRate : distributionRateList) {
                        Account account = distributionRate.getAccount();
                        int stock = allStock * distributionRate.getRate() / 100;
                        ChannelStock channelStock = stockRepository.findByItemAndAccountAndStatus(product,account,"가용재고");
                        channelStock.setStock(stock);
                    }
                }

            }

            return ResultResDataDto.fromResMsg(true, "성공");

        } catch (Exception e) {
            return ResultResDataDto.fromResMsg(false, "에러");

        }
    }

    public boolean checkStock(String accountCode, String productCode, int count) {
        Optional<ChannelStock> channelStock = stockRepository.checkStock(accountCode, productCode, count);
        if (channelStock.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    @Transactional
    public ResultResDataDto fixStock(Order order) {
        try {
            Optional<ChannelStock> channelStock = stockRepository.checkStock(order.getAccount().getAccountCode(), order.getOrderItems().get(0).getItem().getProductCode(), order.getOrderItems().get(0).getCount());
            if (!channelStock.isEmpty()) {
                ChannelStock channelStock1 = channelStock.get();
                channelStock1.setStock(channelStock1.getStock() - order.getOrderItems().get(0).getCount());

                Item product = order.getOrderItems().get(0).getItem();
                Account account = order.getAccount();
                ChannelStock newStock = stockRepository.findByItemAndAccountAndStatus(product,account,"홀딩");
                if(newStock==null){
                    newStock = new ChannelStock();
                    newStock.setStock(order.getOrderItems().get(0).getCount());
                    newStock.setStatus("홀딩");
                    newStock.setItem(product);
                    newStock.setAccount(account);
                }else{
                    newStock.setStock(newStock.getStock()+order.getOrderItems().get(0).getCount());
                }
                stockRepository.save(newStock);
            } else {
                return ResultResDataDto.fromResMsg(false, "재고가 없어ㅆ");
            }
            return ResultResDataDto.fromResMsg(true, "성공");

        } catch (Exception e) {
            return ResultResDataDto.fromResMsg(false, "실패");

        }
    }
}
