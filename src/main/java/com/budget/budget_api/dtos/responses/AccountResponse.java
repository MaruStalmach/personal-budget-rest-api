package com.budget.budget_api.dtos.responses;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String name,
        BigDecimal balance
) {}