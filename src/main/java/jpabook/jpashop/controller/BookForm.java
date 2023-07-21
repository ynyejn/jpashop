package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookForm {
    //상품은 상품수량때문에 id값이 있어야함
    private Long id;
    private String name;
    private Long price;
    private Long stockQuantity;
    private String author;
    private String isbn;
}
