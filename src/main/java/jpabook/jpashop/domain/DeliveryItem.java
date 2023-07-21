package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
public class DeliveryItem {

    @Id
    @GeneratedValue
    @Column(name = "deliveryItem_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)  //다대일
    @JoinColumn(name = "delivery_id") //다에 외래키가 들어감
    private Delivery delivery;

    @JsonIgnore
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "orderItem_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PRODUCT_CODE",referencedColumnName = "PRODUCT_CODE")
    private Item item;

    private Long count;//주문수량

    public static DeliveryItem createDeliveryItem(OrderItem orderItem) {
        DeliveryItem deliveryItem = new DeliveryItem();
        deliveryItem.setOrderItem(orderItem);
        deliveryItem.setItem(orderItem.getItem());
        deliveryItem.setCount(1L);
        return deliveryItem;
    }
}
