package jpabook.jpashop.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OrderForm {
    private String accountCode;
    private Long memberId;
    private List<OrderItemDto> item;
}
