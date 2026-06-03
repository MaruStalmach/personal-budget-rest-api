package com.budget.budget_api.account;

import com.budget.budget_api.account.dto.AccountRequest;
import com.budget.budget_api.account.dto.AccountResponse;
import com.budget.budget_api.common.exception.AccountHasTransactionsException;
import com.budget.budget_api.common.exception.ResourceNotFoundException;
import com.budget.budget_api.transaction.Transaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    private Account mockAccount;

    @BeforeEach
    void setUpMockAccount() {
        mockAccount = new Account();
        Long accountId = 1L;
        mockAccount.setId(accountId);

        mockAccount.setName("mock account");
        mockAccount.setBalance(BigDecimal.ZERO);
        mockAccount.setTransactions(new ArrayList<>());

    }

    @Test
    @DisplayName("should map and return an account when ID exists in the db")
    void getAccountById_Successful() {
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(mockAccount));

        AccountResponse response = accountService.getAccountById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("mock account", response.name());

        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when id does not exist in the db")
    void getAccountById_Unsuccessful() {
        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(mockAccount));

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(2L));
        verify(accountRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("should save entity and return correctly mapped dto")
    void createNewAccount_Success() {
        AccountRequest request = new AccountRequest("savings account");

        Account savedAccount = new Account();
        savedAccount.setId(2L);
        savedAccount.setName("savings account");
        savedAccount.setBalance(BigDecimal.ZERO);

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
    }


    @Test
    void deleteAccountById_ShouldDelete_WhenNoTransactions(){
        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));

        accountService.deleteAccountById(1L);
        verify(accountRepository, times(1)).delete(any());

    }

    @Test
    void deleteAccountById_ShouldNotDelete_WhenTransactionsExist() {
        mockAccount.getTransactions().add(new Transaction());

        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));
        assertThrows(AccountHasTransactionsException.class, () -> accountService.deleteAccountById(1L));
        verify(accountRepository, never()).delete(any());
    }

}