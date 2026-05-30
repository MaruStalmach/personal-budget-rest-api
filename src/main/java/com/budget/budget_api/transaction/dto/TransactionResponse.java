package com.budget.budget_api.transaction.dto;

import com.budget.budget_api.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long accountId,
        BigDecimal amount,
        TransactionType type,
        String category,
        String description,
        LocalDateTime transactionTime

) {
}
