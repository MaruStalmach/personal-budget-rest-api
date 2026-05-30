package com.budget.budget_api.account;

import com.budget.budget_api.account.dto.AccountRequest;
import com.budget.budget_api.account.dto.AccountResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(account.getId(), account.getName(), account.getBalance());
    }

    public List<AccountResponse> getAllAccounts() {
        List<Account> accountList = accountRepository.findAll();
        return accountList.stream().map(this::mapToResponse).toList();
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("an acount of a given id does not exist"));
    }

    public AccountResponse createNewAccount(AccountRequest request) {
        Account newAccount = new Account();
        newAccount.setName(request.name());

        Account savedAccount = accountRepository.save(newAccount);

        return mapToResponse(savedAccount);
    }

    public void deleteAccountById(Long id) {
        Account account = getAccountById(id);
        accountRepository.delete(account);
    }

}
