package com.budget.budget_api.transaction.dto;

import com.budget.budget_api.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionRequest(
    @NotNull(message = "account name must be specified")
    Long accountId,

    @NotNull(message = "transaction amount is required")
    @Positive(message = "transaction amount must be bigger than 0")
    BigDecimal amount,

    @NotNull(message = "transaction must either be an INCOME or an EXPENSE")
    TransactionType type,

    @NotBlank(message = "transaction category can not be empty")
    @Size(max = 50, message = "category name cannot exceed 50 characters")
    String category,

    @Size(max = 255, message = "description cannot exceed 255 characters")
    String description

) {}

