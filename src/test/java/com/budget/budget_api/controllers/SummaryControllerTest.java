package com.budget.budget_api.controllers;

import com.budget.budget_api.common.types.TransactionType;
import com.budget.budget_api.entities.Account;
import com.budget.budget_api.integration.AbstractIntegrationTest;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SummaryControllerTest extends AbstractIntegrationTest {

    private BigDecimal decimalAt(JsonNode node, String field) {
        // asString() gives the textual number (Jackson 3 renamed asText() -> asString()),
        // so BigDecimal comparison stays scale-independent.
        return new BigDecimal(node.get(field).asString());
    }

    @Test
    @DisplayName("GET /accounts/{id}/summary -> 200, totals and per-category expense breakdown")
    void summary_returnsTotalsAndBreakdown() throws Exception {
        Account account = persistAccount("Main");
        LocalDateTime now = LocalDateTime.now();
        persistTransaction(account, "1000.00", TransactionType.INCOME, "Salary", now);
        persistTransaction(account, "500.00", TransactionType.INCOME, "Bonus", now);
        persistTransaction(account, "200.00", TransactionType.EXPENSE, "Food", now);
        persistTransaction(account, "100.00", TransactionType.EXPENSE, "Food", now);
        persistTransaction(account, "150.00", TransactionType.EXPENSE, "Transport", now);

        MvcResult result = mockMvc.perform(get("/accounts/{id}/summary", account.getId()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = body(result);
        assertThat(decimalAt(json, "totalIncome")).isEqualByComparingTo("1500.00");
        assertThat(decimalAt(json, "totalExpenses")).isEqualByComparingTo("450.00");

        JsonNode byCategory = json.get("expensesByCategory");
        assertThat(new BigDecimal(byCategory.get("Food").asString())).isEqualByComparingTo("300.00");
        assertThat(new BigDecimal(byCategory.get("Transport").asString())).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("GET /accounts/{id}/summary for an account with no transactions -> zeros and empty breakdown")
    void summary_emptyAccount_returnsZeros() throws Exception {
        Account account = persistAccount("Empty");

        MvcResult result = mockMvc.perform(get("/accounts/{id}/summary", account.getId()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = body(result);
        assertThat(decimalAt(json, "totalIncome")).isEqualByComparingTo("0");
        assertThat(decimalAt(json, "totalExpenses")).isEqualByComparingTo("0");
        assertThat(json.get("expensesByCategory").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("GET /accounts/{id}/summary for unknown account -> 404 with error body")
    void summary_unknownAccount_returns404() throws Exception {
        mockMvc.perform(get("/accounts/{id}/summary", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpectAll(errorEnvelope(HttpStatus.NOT_FOUND, "/accounts/" + NON_EXISTENT_ID + "/summary"))
                .andExpect(jsonPath("$.message", containsString("cannot be found")));
    }
}