package jpabook.jpashop.repository.order.simplequery;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {
    private final EntityManager em;
    public List<OrderSimpleQueryDto> findOrderDtos() {  //원하는것만 select, 근데좀 별로 repository순수성떨어짐.. repository가 화면을의존..api스펙에맞춰있어서..그래서별도분리
        return em.createQuery("select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id,m.name,o.orderDate,o.status,d.address) " +
                        "from Order o "+
                        " join o.member m" +
                        " join o.delivery d",OrderSimpleQueryDto.class)
                .getResultList();
    }
}
