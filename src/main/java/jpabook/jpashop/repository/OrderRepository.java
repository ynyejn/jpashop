package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jpabook.jpashop.api.OrderSimpleApiController;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.QItem;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {  //repository 는 가급적 순수한 entity를 조회하는데 써야함
    private final EntityManager em;

    public void save(Order order){
       em.persist(order);
    }
    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    /**
     * 문자로..if 노가다스
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }


    /**
     * JPA Criteria : 권장방법아님
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대1000건
        return query.getResultList();
    }


    public List<Order> findAll(OrderSearch orderSearch){

        JPAQueryFactory query = new JPAQueryFactory(em);
        QOrder order = QOrder.order;
        QMember member = QMember.member;
        QAccount account = QAccount.account;
        QOrderItem orderItem = QOrderItem.orderItem;
        QItem item = QItem.item;

        //jpql로 바뀌어서 실행됨 자동으로
        return query
                .select(order)
                .from(order)
                .join(order.member , member)
                .join(order.account, account)
                .join(order.orderItems, orderItem)
                .join(orderItem.item,item)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .groupBy(item)
                .limit(1000)
                .fetch();
    }

    private static BooleanExpression nameLike(String memberName) {
        if(!StringUtils.hasText(memberName)){
            return null;
        }
        return QMember.member.name.like(memberName);
    }

    private BooleanExpression statusEq(OrderStatus status){
        if(status==null){
            return null;
        }else{
            return QOrder.order.status.eq(status);
        }
    }
    public List<Order> findAllCustom() {
        return em.createQuery("select o from Order o"+
                        " join fetch o.member m" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i",Order.class)
                .getResultList();
    }
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery("select o from Order o"+
                " join fetch o.member m" +
                " join fetch o.delivery d",Order.class)
                .getResultList();
    }

    public List<Order> findAllWithItem() {  //fetch join,, 단점 페이징불가능..1:다에서 fetch join쓰면..페이징안됨
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i",Order.class
        ).setFirstResult(1).setMaxResults(100).getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {   //toOne관계는 왠만하면 fetch join
        return em.createQuery("select o from Order o"+
                        " join fetch o.member m" +
                        " join fetch o.delivery d",Order.class)
                .setFirstResult(offset).setMaxResults(limit).getResultList();
    }

}
