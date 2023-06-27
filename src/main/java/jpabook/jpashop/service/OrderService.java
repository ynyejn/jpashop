package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.*;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final AccountRepository accountRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(String accountCode, Long memberId, Long itemId, int count){

        //엔티티조회
        Member member = memberRepository.findById(memberId).get();
        Item item = itemRepository.findOne(itemId);
        Account account = accountRepository.findByAccountCode(accountCode);

        //배송정보 설정
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        //주문상품 생성
        //Order 객체에 OrderItem이 cascade처리되어있어 오더객체를 persist하면 그안에있는 OrderItem도 persist됨, 그래서 orderItemRepository불필요
        //Delivery도 마찬가지,
        //근데 cascade는 막 쓰면 안됨 다른데서 다 쓰이는 애를 cascade하면 안되니까 잘 생각해서 해야 함
        //order와 delivery와 orderitem은 liftcycle이 완전히 같기 때문에 사용할 수 있다
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);//예시는 오더아이템하나만 넘길수있도록만듬

        //주문생성
        Order order = Order.createOrder(account,member, delivery, orderItem);

        //주문저장
        orderRepository.save(order);
        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId){
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancel();
    }

    //검색
    public List<Order> searchOrder(OrderSearch orderSearch){
//        List<Order> orders = orderRepository.findAllByString(orderSearch);
        List<Order> orders = orderRepository.findAll(orderSearch);
        return orders;
    }

}
