package com.budget.budget_api.entities;

import com.budget.budget_api.common.exception.InvalidTransactionException;
import com.budget.budget_api.common.types.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setBalance(new BigDecimal("4200.00"));
    }

    @Test
    @DisplayName("applyTransaction -> INCOME increases balance")
    void applyTransaction_Income_IncreasesBalance() {
        account.applyTransaction(TransactionType.INCOME, new BigDecimal("10.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("4210.00");
    }

    @Test
    @DisplayName("applyTransaction -> EXPENSE decreases balance")
    void applyTransaction_Expense_DecreasesBalance() {
        account.applyTransaction(TransactionType.EXPENSE, new BigDecimal("10.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("4190.00");
    }

    @Test
    @DisplayName("applyTransaction -> zero or negative amount throws InvalidTransactionException")
    void applyTransaction_InvalidAmount_ThrowsException() {
        assertThatThrownBy(() -> account.applyTransaction(TransactionType.INCOME, BigDecimal.ZERO))
                .isInstanceOf(InvalidTransactionException.class);

        assertThatThrownBy(() -> account.applyTransaction(TransactionType.EXPENSE, BigDecimal.ZERO))
                .isInstanceOf(InvalidTransactionException.class);

        assertThatThrownBy(() -> account.applyTransaction(TransactionType.INCOME, new BigDecimal("-5.00")))
                .isInstanceOf(InvalidTransactionException.class);
    }

    @Test
    @DisplayName("revertTransaction -> INCOME decreases balance (reverts the addition)")
    void revertTransaction_Income_DecreasesBalance() {
        account.revertTransaction(TransactionType.INCOME, new BigDecimal("50.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("4150.00");
    }

    @Test
    @DisplayName("revertTransaction -> EXPENSE increases balance (reverts the deduction)")
    void revertTransaction_Expense_IncreasesBalance() {
        account.revertTransaction(TransactionType.EXPENSE, new BigDecimal("30.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("4230.00");
    }
}