package jpabook.jpashop.controller;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.naming.Binding;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")     //Get으로 넘어오면이거탐
    public String createForm(Model model){
        model.addAttribute("memberForm",new MemberForm());
        return "members/createMemberForm"; //화면명 html
    }

    @PostMapping("/members/new")    //Post로 넘어오면 이거탐
    public String create(@Valid MemberForm form, BindingResult result){ //바인딩리절트있으면 밸리데이션오류가 리절트에 담겨서 실행됨 튕기지않고..
        //폼은따로 폼객체를 만들어서 하는게 좋음
        if(result.hasErrors()){
            return "members/createMemberForm";  //result를 가지고 화면으로감
        }
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/"; //첫번째페이지로 redirect
    }
}
