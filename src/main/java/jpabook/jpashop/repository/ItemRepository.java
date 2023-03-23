package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item){
        if(item.getId() == null){
            em.persist(item);
        }else {
            em.merge(item);//update비슷한거
            //변경감지 기능을 사용하면 원하는 속성만 변경할 수 있지만, 병합을 사용하면 모든 속성이 변경된다.
            //병합시 값이 없으면 'null'로 업데이트 할 위험도 있다.(병합은 모든 필드를 교체한다.)
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class,id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i",Item.class).getResultList();//jpql 객체를 대상으로 쿼리돌림
    }

}
