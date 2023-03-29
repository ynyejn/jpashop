package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController     //@Controller + @ResponseBody RestAPI스타일로 만들겠다.
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){    //젤안좋은버전 엔티티직접반환 최악
        List<Member> members = memberService.findMembers();
        return members;
    }
    @GetMapping("/api/v2/members")
    public Result membersV2(){
        List<Member> members = memberService.findMembers();
        List<MemberDto> collect = members.stream().map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result<>(collect.size(),collect);

    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private int count;  //변수추가도 자유로움
        private T data;     //array를 바로 넘기지않고 data에 담아서 보냄
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }
    @PostMapping("/api/v1/members")
    public  CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        //@RequestBody 붙으면 json으로 온 body를 멤버로 쫙바꿔줌.
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public  CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        //api용 dto를 이용함 멤버entity가 바뀌어도 영향을 받지않는다. entity가 변경되어도 api스펙이 바뀌지않음. api스펙확인도 훨편함(CreateMemberRequest만 까보면됨)
        //반드시 이런식으로 쓰세용~ 엔티티를 함부로 노출하면 안됨. 요청도 응답도!
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}") //수정은 PUT!
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,@RequestBody @Valid UpdateMemberRequest request){
        memberService.update(id, request.getName()); //커맨드와
        Member member = memberService.findOne(id);   //쿼리분리 하는게 유지보수성이 증대됨
        return new UpdateMemberResponse(member.getId(),member.getName());
    }

    @Data
    static class CreateMemberRequest{
        private String name;

    }

    @Data
    static class CreateMemberResponse{
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
    @Data
    static class UpdateMemberRequest{
        private String name;

    }

    @Data
    @AllArgsConstructor //dto에서는 걍이런거씀
    static class UpdateMemberResponse{
        //상황에따라 create랑같이써도되긴함 지금은 좀다르니까이렇게하겟음
        private Long id;
        private String name;
    }
}
