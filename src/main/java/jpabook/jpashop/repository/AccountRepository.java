package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Account findByAccountCode(String accountCode);
}
