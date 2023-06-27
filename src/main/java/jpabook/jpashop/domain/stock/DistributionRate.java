package jpabook.jpashop.domain.stock;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Account;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DISTRIBUTION_RATE")
@Getter
@Setter
public class DistributionRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DR_IDX")
    private Long id;
    @OneToOne
    @JoinColumn(name = "ACCOUNT_CODE",referencedColumnName = "ACCOUNT_CODE")
    private Account account;
    private int rate;
}
