package com.budget.budget_api.services;

import com.budget.budget_api.common.exception.ResourceNotFoundException;
import com.budget.budget_api.common.types.TransactionType;
import com.budget.budget_api.dtos.responses.SummaryResponse;
import com.budget.budget_api.repositories.AccountRepository;
import com.budget.budget_api.repositories.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private SummaryService summaryService;

    @Test
    @DisplayName("returns zeros and an empty breakdown when the account has no transactions")
    void getSummary_noTransactions_returnsZeros() {
        Long accountId = 1L;
        when(accountRepository.existsById(accountId)).thenReturn(true);
        // SUM over zero rows returns null from the database; getSafeSum must convert it to ZERO
        when(transactionRepository.sumAmountByAccountIdAndType(eq(accountId), any())).thenReturn(null);
        when(transactionRepository.sumAmountByCategory(eq(accountId), any())).thenReturn(List.of());

        SummaryResponse summary = summaryService.getSummary(accountId);

        assertThat(summary.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.totalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.expensesByCategory()).isEmpty();
    }

    @Test
    @DisplayName("aggregates income, expenses, and the per-category breakdown")
    void getSummary_withTransactions_aggregates() {
        Long accountId = 1L;
        when(accountRepository.existsById(accountId)).thenReturn(true);
        when(transactionRepository.sumAmountByAccountIdAndType(accountId, TransactionType.INCOME))
                .thenReturn(new BigDecimal("1500.00"));
        when(transactionRepository.sumAmountByAccountIdAndType(accountId, TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("450.00"));
        when(transactionRepository.sumAmountByCategory(accountId, TransactionType.EXPENSE))
                .thenReturn(List.of(
                        new Object[]{"Food", new BigDecimal("300.00")},
                        new Object[]{"Transport", new BigDecimal("150.00")}
                ));

        SummaryResponse summary = summaryService.getSummary(accountId);

        assertThat(summary.totalIncome()).isEqualByComparingTo("1500.00");
        assertThat(summary.totalExpenses()).isEqualByComparingTo("450.00");
        assertThat(summary.expensesByCategory())
                .containsEntry("Food", new BigDecimal("300.00"))
                .containsEntry("Transport", new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("throws ResourceNotFoundException when the account does not exist")
    void getSummary_unknownAccount_throws() {
        Long accountId = 999L;
        when(accountRepository.existsById(accountId)).thenReturn(false);

        assertThatThrownBy(() -> summaryService.getSummary(accountId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}