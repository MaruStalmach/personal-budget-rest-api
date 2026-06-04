package com.budget.budget_api.account;

import com.budget.budget_api.common.exception.InvalidTransactionException;
import com.budget.budget_api.entities.Account;
import com.budget.budget_api.common.types.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUpMockAccount() {
        account = new Account();
    }

    @Test
    void createTransaction_Income(){
        account.setBalance(new BigDecimal("4200.00"));
        account.applyTransaction(TransactionType.INCOME, new BigDecimal("10.00"));
        assertEquals(new BigDecimal("4210.00"), account.getBalance());
    }

    @Test
    void createTransaction_Expense(){
        account.setBalance(new BigDecimal("4200.00"));
        account.applyTransaction(TransactionType.EXPENSE, new BigDecimal("10.00"));
        assertEquals(new BigDecimal("4190.00"), account.getBalance());
    }

    @Test
    void createTransaction_InvalidAmount(){
        account.setBalance(new BigDecimal("4200.00"));
        assertThrows(InvalidTransactionException.class,
                () -> account.applyTransaction(TransactionType.INCOME, BigDecimal.ZERO));

        assertThrows(InvalidTransactionException.class,
                () -> account.applyTransaction(TransactionType.EXPENSE, BigDecimal.ZERO));
    }

    @Test
    void revertTransaction_Income() {
        account.setBalance(new BigDecimal("4200.00"));
        account.revertTransaction(TransactionType.INCOME, new BigDecimal("50.00"));
        assertEquals(new BigDecimal("4150.00"), account.getBalance());

    }

    @Test
    void revertTransaction_Expense(){
        account.setBalance(new BigDecimal("4200.00"));
        account.revertTransaction(TransactionType.EXPENSE, new BigDecimal("30.00"));
        assertEquals(new BigDecimal("4230.00"), account.getBalance());

    }

}