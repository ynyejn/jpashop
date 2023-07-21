package jpabook.jpashop.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateItemDto
{
    private Long id;
    private String name;
    private Long price;
    private Long stockQuantity;

    public UpdateItemDto(Long id, String name, Long price, Long stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
}
