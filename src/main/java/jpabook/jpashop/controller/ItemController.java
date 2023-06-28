package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.UpdateItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model){
        BookForm form = new BookForm();
        model.addAttribute("form",form);
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form){
        Book book = new Book(); //사실 setter안쓰고 book에 생성자 사용하는게 제일깔끔쓰
        book.setName(form.getName());
        book.setId(form.getId());
        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        itemService.saveItem(book);
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String itemList(Model model){
        List<Item> items = itemService.findItems();
        model.addAttribute("items",items);
        return "items/itemList";
    }

    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId,Model model){

        Book book = (Book) itemService.findItem(itemId);
        BookForm form = new BookForm();
        form.setId(book.getId());
        form.setName(book.getName());
        form.setPrice(book.getPrice());
//        form.setStockQuantity(book.getStockQuantity());
        form.setPrice(book.getPrice());
        form.setIsbn(book.getIsbn());
        form.setAuthor(book.getAuthor());

        model.addAttribute("form",form);
        return "items/updateItemForm";
    }

    @PostMapping("items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId,@ModelAttribute("form") BookForm form){

        //실무에서는 id체크도해줘야댐 아무나바꾸면안되니까..
        //book이 지금 준영속엔티티임..이미 DB에 한번 저장되어서 식별자가 존재,임의로 만들어낸 엔티티도 기존식별자를 가지고있으면 준영속엔티티
        //준영속엔티티는 jpa가 관리안함..
//        Book book = new Book();
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());
//        itemService.saveItem(book);                           //book을 어설프게 컨트롤러에서 만들어서 넘긴경우

        itemService.updateItem(new UpdateItemDto(itemId, form.getName(),form.getPrice(),form.getStockQuantity()));

        return "redirect:/items";
    }
}
