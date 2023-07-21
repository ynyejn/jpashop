package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //createOrder아니면 생성불가
public class Order {
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "ACCOUNT_CODE",referencedColumnName = "ACCOUNT_CODE")
    private Account account;

    @ManyToOne(fetch = LAZY)  //오더랑 멤버는 다대일 관계..
    @JoinColumn(name = "member_id") //맵핑을 뭘로할거냐 외래키이름이 member_id 얘가 "연관관계 주인"
    private Member member;

    @BatchSize(size = 1000) //이렇게 개별적으로도 적용가능 in () size 근데 거의 application.yml에 설정하면댐..!
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)//cascade 여기리스트에만 넣어도 orderItem insert됨 delete도같이됨
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY , cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")   //"연관관계 주인"
    private Delivery delivery;

    private LocalDateTime orderDate;//주문시간
    @Enumerated(EnumType.STRING)//ORDINAL은 상태추가되면 유동적이지가 않음 절대쓰면안됨
    private OrderStatus status; //주문상태[ORDER, CANCEL]



    //======연관관계 편의 메소드======// 양방향일때 쓰면좋음
    public void setMember(Member member){    //order.setMember(member); 이런식으로만 해도 멤버에 오더리스트에 해당오더가 들어가는 편의메소드
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

    //======생성 메소드======//
//    public static Order createOrder(Account account, Member member, OrderItem... orderItems){
    public static Order createOrder(Account account, Member member, List<OrderItem> orderItems){
        Order order = new Order();
        order.setAccount(account);
        order.setMember(member);
//        order.setDelivery(delivery);
        for (OrderItem orderItem:orderItems){
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //======비즈니스로직======//
    /**
     * 주문 취소
     */
    public void cancel(){
        if(delivery.getStatus()==DeliveryStatus.COMP){//배송완료
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem:orderItems){
            orderItem.cancel();
        }
    }

    //======조회로직======//
    /**
     * 전체 주문가격 조회
     */
    public Long getTotalPrice(){
        Long totalPrice= orderItems.stream()
                .mapToLong(OrderItem::getTotalPrice).sum();
        return totalPrice;
    }

}
