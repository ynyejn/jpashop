package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable  //jpa의 내장타입이라는뜻 어딘가에 내장이 될 수 있다.
@Getter @Setter
public class Address {

    private String city;
    private String street;
    private String zipcode;
}
