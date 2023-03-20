package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded//내장타입을 포함했다.
    private Address address;

    @OneToMany(mappedBy = "member")  //멤버의 입장에서 오더는 일대다 나는 맵핑된 거울일뿐이야~(mappedBy) 온리 조회용
    private List<Order> orders = new ArrayList<>();
}
