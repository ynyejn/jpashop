package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "A_IDX")
    private Long id;
    @Column(name = "ACCOUNT_CODE",nullable = false,unique = true)
    private String accountCode;
    private String name;
}
