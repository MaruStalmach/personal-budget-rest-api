package com.budget.budget_api.summary.dto;

import java.math.BigDecimal;
import java.util.Map;

public record SummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        Map<String, BigDecimal> expensesByCategory
) {
}
