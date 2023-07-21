package jpabook.jpashop.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDto {
    private Long id;
    private String accountName;
    private String memberName;
    private LocalDateTime regdate;
    private List<OrderItemDto> orderItems;

}
