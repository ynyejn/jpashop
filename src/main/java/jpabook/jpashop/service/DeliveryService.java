package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.DeliveryStatus;
import jpabook.jpashop.dto.ResultResDataDto;
import jpabook.jpashop.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
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
}
