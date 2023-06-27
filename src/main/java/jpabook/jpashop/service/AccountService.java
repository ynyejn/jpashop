package jpabook.jpashop.service;

import jpabook.jpashop.domain.Account;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    public List<Account> findAccounts() {
        return accountRepository.findAll();
    }
}
