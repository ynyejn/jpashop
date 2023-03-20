package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable  //jpa의 내장타입이라는뜻 어딘가에 내장이 될 수 있다.
@Getter //값타입은 변경 불가능하게 설계해야함. 생성할때만 값지정되게
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected Address() {//기본생성자! 퍼블릭보단 프로텍티드가 안전
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
