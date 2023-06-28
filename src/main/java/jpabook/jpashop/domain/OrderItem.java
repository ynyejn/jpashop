package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;

import static jakarta.persistence.FetchType.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)//밑에 생성 메소드가 아니면 따로생성할수없게 protected로
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PRODUCT_CODE",referencedColumnName = "PRODUCT_CODE")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)  //다대일
    @JoinColumn(name = "order_id") //다에 외래키가 들어감
    private Order order;

    private int orderPrice;//주문가격
    private int count;//주문수량



    //======생성 메소드======//
    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

//        item.removeStock(count);    //아이템 재고제거
        return orderItem;
    }

    //비즈니스로직
    public void cancel() {

//        getItem().addStock(count);
    }

    //조회로직

    /**
     * 주문상품 전체 가격 조회
     */
    public int getTotalPrice() {
        return getOrderPrice()*getCount();
    }
}
