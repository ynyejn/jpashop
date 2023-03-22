package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository //jpa활용..
@RequiredArgsConstructor    //lombok=> final필드 생성자 자동생성 후 autowired
public class MemberRepository {

//    @PersistenceContext //엔티티매니저팩토리에서 꺼내고 이럴필요없이 그냥 알아서다됨, 근데 스프링데이터 jpa에서 @Autowired로 대체되기때문에 롬복그거가능
    private final EntityManager em;

    public void save(Member member){    //멤버저장
        em.persist(member);
    }

    public Member findOne(Long id){     //특정멤버조회
        return em.find(Member.class,id);
    }

    public List<Member> findAll(){      //멤버전체조회
        return em.createQuery("select m from Member m",Member.class).getResultList();//jpql 객체를 대상으로 쿼리돌림
    }

    public List<Member> findByName(String name){    //파라미터 바인딩해서 특정회원만 이름으로 찾음
        return em.createQuery("select m from Member m where m.name= :name",Member.class)
                .setParameter("name",name)
                .getResultList();
    }
}
