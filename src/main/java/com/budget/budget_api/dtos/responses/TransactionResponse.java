package com.budget.budget_api.dtos.responses;

import com.budget.budget_api.common.types.TransactionType;

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
