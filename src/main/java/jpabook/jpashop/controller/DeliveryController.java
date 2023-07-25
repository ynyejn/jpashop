package jpabook.jpashop.controller;

import jpabook.jpashop.domain.QDeliveryItem;
import jpabook.jpashop.dto.ResultResDataDto;
import jpabook.jpashop.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PostMapping("/delivery/{deliveryId}/finish")
    @ResponseBody
    private ResultResDataDto finishDelivery(@PathVariable("deliveryId") Long deliveryId){
        return deliveryService.finishDelivery(deliveryId);
    }
    @PostMapping("/delivery/{deliveryId}/{itemId}/return")
    @ResponseBody
    private ResultResDataDto returnDelivery(@PathVariable("deliveryId") Long deliveryId,@PathVariable("itemId") Long itemId){
        return deliveryService.returnDelivery(deliveryId,itemId);
    }
}
