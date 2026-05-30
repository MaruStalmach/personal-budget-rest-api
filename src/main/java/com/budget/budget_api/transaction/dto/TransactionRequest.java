package com.budget.budget_api.transaction.dto;

import com.budget.budget_api.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
    @NotNull
    Long accountId,

    @NotNull @Positive
    BigDecimal amount,

    @NotNull
    TransactionType type,

    @NotBlank
    String category,

    String description

) {}

