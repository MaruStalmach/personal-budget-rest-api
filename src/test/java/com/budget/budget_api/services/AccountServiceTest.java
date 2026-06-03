package com.budget.budget_api.services;

import com.budget.budget_api.common.exception.AccountHasTransactionsException;
import com.budget.budget_api.common.types.TransactionType;
import com.budget.budget_api.entities.Account;
import com.budget.budget_api.entities.Transaction;
import com.budget.budget_api.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private AccountService accountService;

    @Test
    void deleteAccountById_ShouldDelete_WhenNoTransactions() {
        Long accountId = 1L;
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        mockAccount.setName("mock account");
        mockAccount.setBalance(BigDecimal.ZERO);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        accountService.deleteAccountById(accountId);
        verify(accountRepository, times(1)).delete(mockAccount);
    }

    @Test
    void deleteAccountById_ShouldNotDelete_WhenTransactionsPresent() {
        Long accountId = 1L;
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        mockAccount.setName("mock account");
        mockAccount.setBalance(BigDecimal.ZERO);
        mockAccount.setTransactions(new ArrayList<>());

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccount(mockAccount);
        transaction.setAmount(BigDecimal.TEN);
        transaction.setType(TransactionType.INCOME);
        transaction.setCategory("food");
        transaction.setDescription("dinner");
        transaction.setTransactionTime(LocalDateTime.now());

        mockAccount.getTransactions().add(transaction);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        AccountHasTransactionsException exception = assertThrows(
                AccountHasTransactionsException.class,
                () -> accountService.deleteAccountById(accountId)
        );

        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, never()).delete(any()); //ensuring delete was never called
    }



    @Test
    void getAccountById_ShouldGet_WhenAccountExists() {

        Long accountId = 1L;
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        mockAccount.setName("mock account");
        mockAccount.setBalance(BigDecimal.ZERO);
        mockAccount.setTransactions(new ArrayList<>());


        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(mockAccount));

        verify(accountRepository, times(1)).delete(mockAccount);
    }

    @Test
    void createNewAccount() {
        mock();
    }

}