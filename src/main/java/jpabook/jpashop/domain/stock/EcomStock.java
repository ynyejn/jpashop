package jpabook.jpashop.domain.stock;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Account;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "ECOM_STOCK")
@Getter
@Setter
public class EcomStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ES_IDX")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_CODE",referencedColumnName = "ACCOUNT_CODE")
    private Account account;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PRODUCT_CODE",referencedColumnName = "PRODUCT_CODE")
    private Item item;
    private int stock;
    private String status;

}
