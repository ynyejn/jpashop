package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.DeliveryItem;
import jpabook.jpashop.domain.DeliveryStatus;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.dto.ResultResDataDto;
import jpabook.jpashop.repository.DeliveryItemRepository;
import jpabook.jpashop.repository.DeliveryRepository;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryItemRepository deliveryItemRepository;
    private final ItemRepository itemRepository;
    private final StockService stockService;

    @Transactional
    public ResultResDataDto finishDelivery(Long deliveryId) {
        try {
            Optional<Delivery> optionalDelivery = deliveryRepository.findById(deliveryId);
            if(!optionalDelivery.isEmpty()){
                Delivery delivery = optionalDelivery.get();
                delivery.setStatus(DeliveryStatus.COMP);
                stockService.finishDelivery(delivery);
                return ResultResDataDto.fromResMsg(true, "성공");
            }else{
                return ResultResDataDto.fromResMsg(false, "딜리버리없어영");
            }
        }catch (Exception e){
            return ResultResDataDto.fromResMsg(false, "실패");
        }
    }

    @Transactional
    public ResultResDataDto returnDelivery(Long deliveryId, Long itemId) { //itemId로 하면안될듯 어쨋든 아이템 그거필요함...
        try {
            Optional<Delivery> optionalDelivery = deliveryRepository.findById(deliveryId);
            if(!optionalDelivery.isEmpty()){
                Delivery originalDelivery = optionalDelivery.get();
                Item item = itemRepository.findOne(itemId);
                DeliveryItem originalDeliveryItem = deliveryItemRepository.findByDeliveryAndItem(originalDelivery,item);
                OrderItem orderItem = originalDeliveryItem.getOrderItem();

                //기존건 상태변경
                originalDelivery.setStatus(DeliveryStatus.RETURN);

                //반품건 딜리버리 생성
                DeliveryItem newDeliveryItem = DeliveryItem.createDeliveryItem(orderItem);
                Delivery newDelivery = Delivery.createReturnDelivery(originalDelivery.getOrder(), Collections.singletonList(newDeliveryItem));
                deliveryRepository.save(newDelivery);

                return ResultResDataDto.fromResMsg(true, "성공");
            }else{
                return ResultResDataDto.fromResMsg(false, "딜리버리없어영");
            }
        }catch (Exception e){
            return ResultResDataDto.fromResMsg(false, "실패");
        }
    }
}
