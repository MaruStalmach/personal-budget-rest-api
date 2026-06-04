package com.budget.budget_api.services;

import com.budget.budget_api.common.exception.InvalidTransactionException;
import com.budget.budget_api.common.exception.ResourceNotFoundException;
import com.budget.budget_api.common.types.TransactionType;
import com.budget.budget_api.dtos.requests.TransactionRequest;
import com.budget.budget_api.dtos.responses.TransactionResponse;
import com.budget.budget_api.entities.Account;
import com.budget.budget_api.entities.Transaction;
import com.budget.budget_api.repositories.AccountRepository;
import com.budget.budget_api.repositories.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private TransactionService transactionService;

    private Account account() {
        Account account = new Account();
        account.setId(1L);
        account.setName("account " + (Long) 1L);
        account.setBalance(new BigDecimal("100.00"));
        return account;
    }

    private Transaction transaction(Long id, Account account, String amount, TransactionType type, String category) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setAccount(account);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setDescription("description");
        transaction.setTransactionTime(LocalDateTime.now());
        return transaction;
    }

    @Test
    @DisplayName("getAllTransactions maps every entity to a response")
    void getAllTransactions_mapsAll() {
        Account acc = account();
        when(transactionRepository.findAll()).thenReturn(List.of(
                transaction(1L, acc, "10.00", TransactionType.INCOME, "Salary"),
                transaction(2L, acc, "20.00", TransactionType.EXPENSE, "Food")
        ));

        List<TransactionResponse> result = transactionService.getAllTransactions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).accountId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getTransactionById returns the mapped transaction when it exists")
    void getTransactionById_found() {
        Account acc = account();
        when(transactionRepository.findById(5L))
                .thenReturn(Optional.of(transaction(5L, acc, "30.00", TransactionType.EXPENSE, "Food")));

        TransactionResponse response = transactionService.getTransactionById(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.category()).isEqualTo("Food");
        assertThat(response.amount()).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("getTransactionById throws ResourceNotFoundException when missing")
    void getTransactionById_notFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getTransactionsByAccountId returns mapped transactions for an existing account")
    void getTransactionsByAccountId_found() {
        Account acc = account();
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findByAccountId(1L)).thenReturn(List.of(
                transaction(1L, acc, "10.00", TransactionType.INCOME, "Salary")
        ));

        List<TransactionResponse> result = transactionService.getTransactionsByAccountId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).accountId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getTransactionsByAccountId throws ResourceNotFoundException for an unknown account")
    void getTransactionsByAccountId_accountNotFound() {
        when(accountRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.getTransactionsByAccountId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(transactionRepository, never()).findByAccountId(any());
    }

    @Test
    @DisplayName("getFilteredTransactions delegates to the repository and maps the result")
    void getFilteredTransactions_maps() {
        Account acc = account();
        when(transactionRepository.findFilteredTransactions(any(), any(), any()))
                .thenReturn(List.of(transaction(1L, acc, "20.00", TransactionType.EXPENSE, "Food")));

        List<TransactionResponse> result = transactionService.getFilteredTransactions(null, null, "Food");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).category()).isEqualTo("Food");
    }

    @Test
    @DisplayName("createNewTransaction saves the transaction and updates the account balance")
    void createNewTransaction_success() {
        Account acc = account();
        TransactionRequest request =
                new TransactionRequest(1L, new BigDecimal("50.00"), TransactionType.INCOME, "Salary", "bonus");
        Transaction saved = transaction(10L, acc, "50.00", TransactionType.INCOME, "Salary");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse response = transactionService.createNewTransaction(request);

        assertThat(acc.getBalance()).isEqualByComparingTo("150.00"); // income applied
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.accountId()).isEqualTo(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository).save(acc);
    }

    @Test
    @DisplayName("createNewTransaction throws ResourceNotFoundException for an unknown account")
    void createNewTransaction_accountNotFound() {
        TransactionRequest request =
                new TransactionRequest(99L, new BigDecimal("50.00"), TransactionType.INCOME, "Salary", "bonus");
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createNewTransaction(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createNewTransaction propagates InvalidTransactionException for a non-positive amount")
    void createNewTransaction_invalidAmount() {
        Account acc = account();
        TransactionRequest request =
                new TransactionRequest(1L, BigDecimal.ZERO, TransactionType.EXPENSE, "Food", "desc");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> transactionService.createNewTransaction(request))
                .isInstanceOf(InvalidTransactionException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTransaction reverts the balance, saves the account, and deletes the transaction")
    void deleteTransaction_success() {
        Account acc = account();
        Transaction txn = transaction(5L, acc, "30.00", TransactionType.EXPENSE, "Food");
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(txn));

        transactionService.deleteTransaction(5L);

        assertThat(acc.getBalance()).isEqualByComparingTo("130.00"); // reverting an expense adds it back
        verify(accountRepository).save(acc);
        verify(transactionRepository).delete(txn);
    }

    @Test
    @DisplayName("deleteTransaction throws ResourceNotFoundException when missing")
    void deleteTransaction_notFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(transactionRepository, never()).delete(any());
    }
}