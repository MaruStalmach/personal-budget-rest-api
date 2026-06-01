package com.budget.budget_api.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private  AccountService accountService;

    @Test
    void deleteAccountById() {

        Long accountId = 1L;
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        mockAccount.setName("mock account");
        mockAccount.setBalance(BigDecimal.ZERO);
        mockAccount.setTransactions(new ArrayList<>());

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(mockAccount));

        accountService.deleteAccountById(accountId);

        verify(accountRepository, times(1)).delete(mockAccount);


    }

    @Test
    void deleteAccountById_ShouldDelete_WhenNoTransactions() {

    }



    @Test
    void getAccountById() {

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
        mock()
    }

    @Test
    void deleteAccountById() {
    }
}