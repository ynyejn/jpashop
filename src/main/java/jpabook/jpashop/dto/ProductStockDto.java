package jpabook.jpashop.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductStockDto {
    private String productCode;
    private int stock;
}
