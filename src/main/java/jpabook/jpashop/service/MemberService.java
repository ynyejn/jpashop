package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)  //spring꺼 쓰셈,글고 조회가 많으면 readOnly를 쓰는게 성능최적화에좋음
@RequiredArgsConstructor    //얘를 쓰면 final이 있는 필드만 가지고 생성자를 자동으로 만들어줌 lombok에서
public class MemberService {


    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     */
    @Transactional  //얘는 조회아니니까 기본으로 한번 더 붙여줌
    public Long join(Member member){

        validateDuplicateMember(member);//중복회원검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {   //이거해도 넘어갈수있으니 db에 unique걸어놓거라
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    //회원전체조회
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}
