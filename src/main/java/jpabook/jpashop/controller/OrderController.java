package jpabook.jpashop.controller;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Account;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.dto.OrderDto;
import jpabook.jpashop.dto.OrderForm;
import jpabook.jpashop.dto.ResultResDataDto;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.service.AccountService;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;
    private final AccountService accountService;

    @GetMapping("/order")
    public String createFrom(Model model){
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();
        List<Account> accounts = accountService.findAccounts();

        model.addAttribute("members",members);
        model.addAttribute("items",items);
        model.addAttribute("accounts",accounts);

        return "order/orderForm";
    }

    @PostMapping ("/order")
    public String create(@Valid OrderForm orderForm){
        orderService.order(orderForm.getAccountCode(),orderForm.getMemberId(), orderForm.getItem());
        return "redirect:/orders";
    }


    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch")OrderSearch orderSearch,Model model){
        //ModelAttribute 셋팅하면 model박스에 저절로 담김 저절로 뿌려지고 저절로 담김
        //model.addAttribute("orderSearch",orderSearch); 저절로해줌
        List<OrderDto> orders = orderService.searchOrder(orderSearch);

        model.addAttribute("orders",orders);

        return "order/orderList";
    }

    @PostMapping("/order/{orderId}/fix")
    @ResponseBody
    private ResultResDataDto fixOrder(@PathVariable("orderId") Long orderId){
        return orderService.fixOrder(orderId);
    }

    @PostMapping("/order/{orderId}/finish")
    @ResponseBody
    private ResultResDataDto finishOrder(@PathVariable("orderId") Long orderId){
        return orderService.finishOrder(orderId);
    }

    @PostMapping("/order/{orderId}/deliveryFix")
    @ResponseBody
    private ResultResDataDto fixDelivery(@PathVariable("orderId") Long orderId){
        return orderService.fixDelivery(orderId);
    }
    @PostMapping("/orders/{orderId}/cancel")
    private String cancelOrder(@PathVariable("orderId") Long orderId){
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }
    @PostMapping("/order/{orderId}/cancel")
    @ResponseBody
    private ResultResDataDto cancelOrderNew(@PathVariable("orderId") Long orderId,@RequestParam(value = "mode",required = false) String mode){
        if(StringUtils.isEmpty(mode)||mode.equals("")){  //할당전취소
            return orderService.cancelOrder(orderId);
        }else {
            return orderService.cancelFixOrder(orderId,mode);
        }
    }

}
