package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter //실무에서는 가급적 Getter는 열어두고, Setter는 꼭 필요한 경우에만 사용,
// 엔티티변경시에는 Setter대신 변경 지점이 명확하도록 변경을 위한 비즈니스 메소드를 별도 제공해야함
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded//내장타입을 포함했다.ManytoOne
    private Address address;

    @OneToMany(mappedBy = "member")  //멤버의 입장에서 오더는 일대다 나는 맵핑된 거울일뿐이야~(mappedBy) 온리 조회용
    private List<Order> orders = new ArrayList<>(); //필드에서 바로 초기화 하는것이 안전하다. null문제에서 안전하다.그외에도..ManytoOne
}
