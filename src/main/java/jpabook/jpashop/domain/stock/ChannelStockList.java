package jpabook.jpashop.domain.stock;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Account;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "CHANNEL_STOCK_LIST")
@Getter
@Setter
public class ChannelStockList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CSI_IDX")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "CS_IDX")
    private ChannelStock channelStock;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "SS_IDX")
    private SapStock sapStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_CODE",referencedColumnName = "ACCOUNT_CODE")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_CODE",referencedColumnName = "PRODUCT_CODE")
    private Item item;

    private Long qty;


}
