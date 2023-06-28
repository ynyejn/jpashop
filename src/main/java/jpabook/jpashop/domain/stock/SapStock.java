package jpabook.jpashop.domain.stock;

import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "SAP_STOCK")
@Getter
@Setter
public class SapStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SS_IDX")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PRODUCT_CODE",referencedColumnName = "PRODUCT_CODE")
    private Item item;


    @Column(name = "STOCK")
    private int stock;

    @Column(name = "DISTRIBUTION_FLAG")
    private int distributionFlag;


}