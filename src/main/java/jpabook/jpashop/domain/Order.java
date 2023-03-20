package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne  //오더랑 멤버는 다대일 관계..
    @JoinColumn(name = "member_id") //맵핑을 뭘로할거냐 외래키이름이 member_id 얘가 "연관관계 주인"
    private Member member;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "delivery_id")   //"연관관계 주인"
    private Delivery delivery;

    private LocalDateTime orderDate;//주문시간
    @Enumerated(EnumType.STRING)//ORDINAL은 상태추가되면 유동적이지가 않음 절대쓰면안됨
    private OrderStatus status; //주문상태[ORDER, CANCEL]

}
