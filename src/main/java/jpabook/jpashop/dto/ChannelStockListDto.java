package jpabook.jpashop.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelStockListDto {
    private Long id;
    private String accountName;
    private String productName;
    private Long stock;
    private Long usedStock;
    private Long finishedStock;
    private String status;
}
