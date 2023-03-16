package jpabook.jpashop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;
    public long save(Member member){
        em.persist(member);
        return member.getId(); //커멘드랑 쿼리를 분리해라 아이디만 주는것을 선호
    }

    public Member find(Long id){
        return em.find(Member.class,id);
    }
}
