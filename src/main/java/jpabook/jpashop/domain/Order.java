package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY)  //오더랑 멤버는 다대일 관계..
    @JoinColumn(name = "member_id") //맵핑을 뭘로할거냐 외래키이름이 member_id 얘가 "연관관계 주인"
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)//cascade 여기리스트에만 넣어도 orderItem insert됨 delete도같이됨
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY , cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")   //"연관관계 주인"
    private Delivery delivery;

    private LocalDateTime orderDate;//주문시간
    @Enumerated(EnumType.STRING)//ORDINAL은 상태추가되면 유동적이지가 않음 절대쓰면안됨
    private OrderStatus status; //주문상태[ORDER, CANCEL]

    //======연관관계 편의 메소드======// 양방향일때 쓰면좋음
    public void setMember(){    //order.setMember(member); 이런식으로만 해도 멤버에 오더리스트에 해당오더가 들어가는 편의메소드
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem){  //연관관계 편의메소드의 위치는 핵심적으로 컨트롤 하는쪽이 들고있는게 좋음
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //======연관관계 편의 메소드======//

}
