package com.budget.budget_api.services;

import com.budget.budget_api.common.exception.DuplicateResourceException;
import com.budget.budget_api.entities.Account;
import com.budget.budget_api.repositories.AccountRepository;
import com.budget.budget_api.dtos.requests.AccountRequest;
import com.budget.budget_api.dtos.responses.AccountResponse;

import com.budget.budget_api.common.exception.AccountHasTransactionsException;
import com.budget.budget_api.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("account with a given id cannot be found"));
        return mapToResponse(account);
    }

    public AccountResponse createNewAccount(AccountRequest request) {
        if (accountRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("account with a given name already exists");
        }
        Account newAccount = new Account();
        newAccount.setName(request.name());
        newAccount.setBalance(BigDecimal.ZERO);

        Account savedAccount = accountRepository.save(newAccount);

        return mapToResponse(savedAccount);
    }

    public void deleteAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("account with a given id cannot be found"));

        if (!account.getTransactions().isEmpty()) {
            throw new AccountHasTransactionsException("cannot delete the account - transactions still exist");
        }
        accountRepository.delete(account);
    }

}
