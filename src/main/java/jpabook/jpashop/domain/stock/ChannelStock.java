package jpabook.jpashop.domain.stock;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Account;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "CHANNEL_STOCK")
@Getter
@Setter
public class ChannelStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CS_IDX")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PRODUCT_CODE",referencedColumnName = "PRODUCT_CODE")
    private Item item;
    private Long totalQty;
    private String status;
    private String redistributionFlag;

}
