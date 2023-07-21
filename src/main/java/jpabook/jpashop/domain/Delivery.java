package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
public class Delivery {
    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;
//    @JsonIgnore
//    @OneToOne(mappedBy = "delivery",fetch = LAZY)  //연관관계 거울!
//    private Order order;
    @JsonIgnore
    @ManyToOne(fetch = LAZY)  //다대일
    @JoinColumn(name = "order_id") //다에 외래키가 들어감
    private Order order;

    @BatchSize(size = 1000)
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL)
    private List<DeliveryItem> deliveryItems = new ArrayList<>();

    @Embedded
    private Address address;
    @Enumerated(EnumType.STRING)//ORDINAL은 상태추가되면 유동적이지가 않음 절대쓰면안됨
    private DeliveryStatus status; //READY,COMP


    public static Delivery createDelivery(Order order, List<DeliveryItem> deliveryItems){
        Delivery delivery = new Delivery();
        delivery.setStatus(DeliveryStatus.READY);
        delivery.setOrder(order);
        for (DeliveryItem deliveryItem:deliveryItems){
            delivery.addDeliveryItem(deliveryItem);
        }
        return delivery;
    }
    public void addDeliveryItem(DeliveryItem deliveryItem){  //연관관계 편의메소드의 위치는 핵심적으로 컨트롤 하는쪽이 들고있는게 좋음
        deliveryItems.add(deliveryItem);
        deliveryItem.setDelivery(this);
    }

//    public void addOrderItem(OrderItem orderItem) {
//        this.orderItems.add(orderItem);
//    }
}
