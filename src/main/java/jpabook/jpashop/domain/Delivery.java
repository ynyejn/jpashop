package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
public class Delivery {
    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;
    @OneToOne(mappedBy = "delivery",fetch = LAZY)  //연관관계 거울!
    private Order order;
    @Embedded
    private Address address;
    @Enumerated(EnumType.STRING)//ORDINAL은 상태추가되면 유동적이지가 않음 절대쓰면안됨
    private DeliveryStatus status; //READY,COMP
}
