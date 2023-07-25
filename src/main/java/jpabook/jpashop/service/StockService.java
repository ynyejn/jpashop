package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
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
import org.aspectj.weaver.ast.Or;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockService {
    private final OrderJpaRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StockRepository stockRepository;
    private final StockListRepository stockListRepository;
    private final SapStockRepository sapStockRepository;
    private final DistributionRateRepository distributionRateRepository;
    private final ItemService itemService;
    private final AccountService accountService;
    private final DeliveryRepository deliveryRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private String stockKeyPrefix = "stock:";
    private String finishKeyPrefix = "finish:";


    public List<ChannelStockListDto> findStocks() {
        return stockListRepository.findAll().stream().map(cs -> ChannelStockListDto
                .builder()
                .id(cs.getId())
                .accountName(cs.getAccount().getName())
                .productName(cs.getItem().getName())
                .stock(cs.getQty())
                .usedStock(usedCount(getStockKey(stockKeyPrefix, cs.getAccount().getAccountCode(), cs.getItem().getProductCode())))
                .finishedStock(usedCount(getStockKey(finishKeyPrefix, cs.getAccount().getAccountCode(), cs.getItem().getProductCode())))
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

//                //홀딩재고 계산 (오더테이블에 미할당+상품명조건으로 검색) => 이때 사용중으로 들어감    //수정중
//                List<Order> unfixOrder = orderRepository.findAllByItemAndStatus(product, OrderStatus.ORDER);
//                System.out.println(unfixOrder.size());
//                for (Order order : unfixOrder) {
//                    Long count = order.getOrderItems().get(0).getCount();
//                    if(availableStock-count>=0){
//                        for (int i = 0 ; i< count ; i++){
//                            order.setStatus(OrderStatus.ORDER_FIX);
//                            String accountCode = order.getAccount().getAccountCode();
//                            String productCode = order.getOrderItems().get(0).getItem().getProductCode();
//                            String redisKey = "stock:"+accountCode+productCode;
//
//                            //redis 추가
//                            addOnRedis(redisKey, String.valueOf(order.getId())+(i+1));
//                        }
//                    }
//                }


                //사용중재고 total 계산
                ScanOptions scanOptions = ScanOptions.scanOptions().match(stockKeyPrefix + "*" + product.getProductCode()).count(500).build();
                Cursor<byte[]> keys = redisTemplate.getConnectionFactory().getConnection().scan(scanOptions);
                Long allUsedCount = 0L;
                while (keys.hasNext()) {
                    String key = new String(keys.next());
                    allUsedCount += usedCount(key);
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
                    double rate = distributionRate.getRate() / (double) 100;
                    Long stock = Math.round((allStock - allStockRemainder - allUsedCount) * rate) + usedCount(getStockKey(stockKeyPrefix, account.getAccountCode(), product.getProductCode()));
                    if (remainderStock > 0) {
                        stock++;
                        remainderStock--;
                    }
                    Optional<ChannelStockList> channelStockListOptional = stockListRepository.findByAccountAndItem(account, product);
                    if (channelStockListOptional.isEmpty()) { //기존값 없을경우
                        channelStockList = new ChannelStockList();
                        channelStockList.setChannelStock(channelStock);
                        channelStockList.setSapStock(sapStock);
                        channelStockList.setQty(stock);
                        channelStockList.setAccount(account);
                        channelStockList.setItem(product);
                    } else {
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
            ScanOptions scanOptions = ScanOptions.scanOptions().match(finishKeyPrefix + "*").count(500).build();
            Cursor<byte[]> keys = redisTemplate.getConnectionFactory().getConnection().scan(scanOptions);
            while (keys.hasNext()) {
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
                        .stream().map(s -> ProductStockDto.builder()
                                .productCode(s.getProductCode())
                                .stock(s.getStock()).build()).collect(Collectors.toList());
                for (ProductStockDto stockDto : stockDtos) {
                    int allStock = stockDto.getStock();
                    Item product = itemService.findByProductCode(stockDto.getProductCode());
                    for (DistributionRate distributionRate : distributionRateList) {
                        Account account = distributionRate.getAccount();
                        int stock = allStock * distributionRate.getRate() / 100;
                    }
                }

            }

            return ResultResDataDto.fromResMsg(true, "성공");

        } catch (Exception e) {
            return ResultResDataDto.fromResMsg(false, "에러");

        }
    }

    public boolean checkStock(String accountCode, String productCode, Long count) {
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
    public ResultResDataDto fixStock(Order tmpOrder) {
        try {
            Optional<Order> optionalOrder = orderRepository.findById(tmpOrder.getId());
            if (!optionalOrder.isEmpty()) {
                Order order = optionalOrder.get();
                String accountCode = order.getAccount().getAccountCode();
                List<OrderItem> orderItemList = order.getOrderItems();
                List<DeliveryItem> deliveryItemList = new ArrayList<>();

                //오더아이템들 재고 확인하여 할당 작업
                for (OrderItem orderItem : orderItemList) {
                    System.out.println("지");
                    if (orderItem.getStatus() != OrderItemStatus.ORDER) {
                        continue;
                    }
                    Long count = orderItem.getCount();
                    System.out.println(count + "여기들어옴");
                    String productCode = orderItem.getItem().getProductCode();
                    //findBy해서 account 랑 item으로 하는것으로 수정하자
                    Optional<ChannelStockList> checkStock = stockListRepository.checkStock(accountCode, productCode, count);
                    if (!checkStock.isEmpty()) {
                        //--------------조회시 다른 트랙잭션에서 조회 불가하도록 lock 시작--------------
                        ChannelStockList channelStockList = stockListRepository.findPessimisticById(checkStock.get().getId());
                        Long avail = availableStock(channelStockList.getId());
                        if (avail - count >= 0) {
                            //할당로직
                            System.out.println(orderItem.getId());
                            orderItem.fixOrderItem();
                            System.out.println("여기들오냐");
                            System.out.println(orderItem.getStatus());
//                            orderItemRepository.save(orderItem);

                            DeliveryItem deliveryItem = DeliveryItem.createDeliveryItem(orderItem);
                            deliveryItemList.add(deliveryItem);
                            System.out.println("여기들오냐2");
                            //redis 추가
                            String redisKey = getStockKey(stockKeyPrefix, accountCode, productCode);
                            addOnRedis(redisKey, String.valueOf(orderItem.getId()));

                        } else {
                            System.out.println("여긴가444");
//                            return ResultResDataDto.fromResMsg(false, "재고가 없어요");
                        }
                    } else {
//                        return ResultResDataDto.fromResMsg(false, "재고가 없어요");
                    }
                }
                //전체 할당되었으면 delivery 확정처리
                if (!deliveryItemList.isEmpty()) {
                    Optional<Delivery> optionalDelivery = deliveryRepository.findByOrderAndStatus(order, DeliveryStatus.READY);
                    Delivery delivery = null;

                    //재할당하는경우와 초기할당의 경우 분리
                    if (optionalDelivery.isEmpty()) {
                        delivery = Delivery.createDelivery(order, deliveryItemList);
                    } else {
                        delivery = optionalDelivery.get();
                        for (DeliveryItem deliveryItem : deliveryItemList) {
                            delivery.addDeliveryItem(deliveryItem);
                        }
                    }
                    deliveryRepository.save(delivery);
                    orderRepository.save(order);
                    //할당시 전부할당됐으면 delivery처리
                    if (checkAllocatedStock(order)) {
                        System.out.println("요긔긔");
                        order.fixDeliveries();
                    }else{
                        System.out.println("요긔2");
                    }
                    deliveryRepository.save(delivery);

                    return ResultResDataDto.fromResMsg(true, "성공");
                } else {
                    return ResultResDataDto.fromResMsg(false, "할당된거없슴");
                }
            } else {
                return ResultResDataDto.fromResMsg(false, "오더가없슴");
            }


        } catch (Exception e) {
            throw e;
//            return ResultResDataDto.fromResMsg(false, "실패");

        }
    }

    private boolean checkAllocatedStock(Order order) {
        boolean check = true;
        List<OrderItem> orderItemList = order.getOrderItems();
        for (OrderItem orderItem : orderItemList) {
            if (orderItem.getStatus() == OrderItemStatus.ORDER) {
                check = false;
            }
        }
        return check;
    }

    public Long availableStock(Long key) {
        ChannelStockList channelStockList = stockListRepository.findById(key).get();
        Long allStock = channelStockList.getQty();
        String redisKey = getStockKey(stockKeyPrefix, channelStockList.getAccount().getAccountCode(), channelStockList.getItem().getProductCode());
        Long usedStock = usedCount(redisKey);
        return allStock - usedStock;
    }

    public Long usedCount(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public void addOnRedis(String key, String value) {
        Long addSiz = redisTemplate.opsForSet().add(key, value);
    }

    private void delOnRedis(String key) {
        Boolean delete = redisTemplate.delete(key);
    }

    public void delOnRedis(String key, String value) {
        Long remove = redisTemplate.opsForSet().remove(key, value);
    }

    private String generateKey(Long key) {
        return key.toString();
    }

    public ResultResDataDto finishStock(Order order) {  //레디스 사용중인거에서 빼고 완료에 넣어야됨
        try {
            //딜리버리 나눠진화면에따라..딜리버리넘버로 배송완료처리해야댐..
            Long count = order.getOrderItems().get(0).getCount();
            String accountCode = order.getAccount().getAccountCode();
            String productCode = order.getOrderItems().get(0).getItem().getProductCode();
            String redisFinishKey = getStockKey(finishKeyPrefix, accountCode, productCode);
            String redisKey = getStockKey(stockKeyPrefix, accountCode, productCode);
            delOnRedis(redisKey, String.valueOf(order.getId()) + 1);
            addOnRedis(redisFinishKey, String.valueOf(order.getId()) + 1);

            return ResultResDataDto.fromResMsg(true, "성공");
        } catch (Exception e) {
            return ResultResDataDto.fromResMsg(false, "실패");
        }
    }

    @Transactional
    public ResultResDataDto fixDelivery(Order order) {
        try {
            Optional<Delivery> optionalDelivery = deliveryRepository.findByOrderAndStatus(order, DeliveryStatus.READY);
            if (!optionalDelivery.isEmpty()) {
                Delivery delivery = optionalDelivery.get();
                delivery.setStatus(DeliveryStatus.FIX);
                return ResultResDataDto.fromResMsg(true, "성공");
            } else {
                return ResultResDataDto.fromResMsg(false, "일부할당할게 없어영");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public ResultResDataDto finishDelivery(Delivery tmpDelivery) {
        try {
            Optional<Delivery> optionalDelivery = deliveryRepository.findById(tmpDelivery.getId());
            if (!optionalDelivery.isEmpty()) {
                Delivery delivery = optionalDelivery.get();
                Order order = delivery.getOrder();
                String accountCode = order.getAccount().getAccountCode();

                List<DeliveryItem> deliveryItems = delivery.getDeliveryItems();
                for (DeliveryItem deliveryItem : deliveryItems) {
                    String productCode = deliveryItem.getItem().getProductCode();
                    String redisFinishKey = getStockKey(finishKeyPrefix, accountCode, productCode);
                    String redisKey = getStockKey(stockKeyPrefix, accountCode, productCode);
                    delOnRedis(redisKey, String.valueOf(deliveryItem.getOrderItem().getId()));
                    addOnRedis(redisFinishKey, String.valueOf(deliveryItem.getOrderItem().getId()));
                }
                return ResultResDataDto.fromResMsg(true, "성공");
            } else {
                return ResultResDataDto.fromResMsg(false, "일부할당할게 없어영");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private String getStockKey(String prefix, String accountCode, String productCode) {
        return prefix + accountCode + ":" + productCode;
    }

    public void cancelStock(Order order) {
        List<OrderItem> orderItemList = order.getOrderItems();
        for (OrderItem orderItem : orderItemList) {
            String redisKey = getStockKey(stockKeyPrefix, order.getAccount().getAccountCode(), orderItem.getItem().getProductCode());
            delOnRedis(redisKey, String.valueOf(orderItem.getId()));
        }
    }
}
