package jpabook.jpashop.dto;

import jpabook.jpashop.domain.OrderItemStatus;
import jpabook.jpashop.domain.item.Item;
import lombok.*;

@Data
@AllArgsConstructor
public class OrderItemDto {
    private Item item;
    private Long itemPrice;
    private Long itemCount;
    private OrderItemStatus itemStatus;

}
