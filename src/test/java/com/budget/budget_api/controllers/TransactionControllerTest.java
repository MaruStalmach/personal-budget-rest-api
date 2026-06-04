package com.budget.budget_api.controllers;

import com.budget.budget_api.common.types.TransactionType;
import com.budget.budget_api.entities.Account;
import com.budget.budget_api.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerTest extends AbstractIntegrationTest {

    private Map<String, Object> transactionRequest(Long accountId, String amount, String type, String category) {
        Map<String, Object> request = new HashMap<>();
        request.put("accountId", accountId);
        request.put("amount", amount == null ? null : new BigDecimal(amount));
        request.put("type", type);
        request.put("category", category);
        request.put("description", "integration test");
        return request;
    }

    private long postTransaction(Long accountId, String amount, String type, String category) throws Exception {
        MvcResult result = mockMvc.perform(post("/transactions")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(transactionRequest(accountId, amount, type, category))))
                .andExpect(status().isCreated())
                .andReturn();
        return body(result).get("id").asLong();
    }

    @Test
    @DisplayName("POST /transactions INCOME -> 201, increases account balance")
    void createIncome_increasesBalance() throws Exception {
        Account account = persistAccount("Main");

        mockMvc.perform(post("/transactions")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(transactionRequest(account.getId(), "200.00", "INCOME", "Salary"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.accountId").value(account.getId().intValue()))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.category").value("Salary"));

        Account reloaded = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(reloaded.getBalance()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("INCOME then EXPENSE -> balance reflects both operations")
    void incomeThenExpense_updatesBalance() throws Exception {
        Account account = persistAccount("Main");

        postTransaction(account.getId(), "300.00", "INCOME", "Salary");
        postTransaction(account.getId(), "100.00", "EXPENSE", "Food");

        Account reloaded = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(reloaded.getBalance()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("EXPENSE on a zero-balance account -> 201, balance goes negative (documents current behaviour)")
    void expenseBelowZero_isCurrentlyAllowed() throws Exception {
        Account account = persistAccount("Main");

        postTransaction(account.getId(), "50.00", "EXPENSE", "Food");

        Account reloaded = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(reloaded.getBalance()).isEqualByComparingTo("-50.00");
    }

    @Test
    @DisplayName("POST /transactions with non-positive amount -> 400, no balance change")
    void createTransaction_nonPositiveAmount_returns400() throws Exception {
        Account account = persistAccount("Main");

        for (String amount : new String[]{"0.00", "-50.00"}) {
            mockMvc.perform(post("/transactions")
                            .contentType(APPLICATION_JSON)
                            .content(toJson(transactionRequest(account.getId(), amount, "EXPENSE", "Food"))))
                    .andExpect(status().isBadRequest())
                    .andExpectAll(errorEnvelope(HttpStatus.BAD_REQUEST, "/transactions"));
        }

        Account reloaded = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(reloaded.getBalance()).isEqualByComparingTo("0");
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /transactions with missing required fields -> 400 with validation error body")
    void createTransaction_missingFields_returns400() throws Exception {
        Account account = persistAccount("Main");

        //missing type
        mockMvc.perform(post("/transactions")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(transactionRequest(account.getId(), "10.00", null, "Food"))))
                .andExpect(status().isBadRequest())
                .andExpectAll(errorEnvelope(HttpStatus.BAD_REQUEST, "/transactions"))
                .andExpect(jsonPath("$.message", containsString("validation")));

        //missing category
        mockMvc.perform(post("/transactions")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(transactionRequest(account.getId(), "10.00", "EXPENSE", null))))
                .andExpect(status().isBadRequest())
                .andExpectAll(errorEnvelope(HttpStatus.BAD_REQUEST, "/transactions"))
                .andExpect(jsonPath("$.message", containsString("validation")));
    }

    @Test
    @DisplayName("POST /transactions for an unknown account -> 404 with error body")
    void createTransaction_unknownAccount_returns404() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(transactionRequest(NON_EXISTENT_ID, "10.00", "EXPENSE", "Food"))))
                .andExpect(status().isNotFound())
                .andExpectAll(errorEnvelope(HttpStatus.NOT_FOUND, "/transactions"))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("GET /transactions without filters -> 200, returns all transactions")
    void getAll_noFilters_returnsAll() throws Exception {
        Account account = persistAccount("Main");
        persistTransaction(account, "100.00", TransactionType.INCOME, "Salary", LocalDateTime.of(2024, 1, 10, 10, 0));
        persistTransaction(account, "30.00", TransactionType.EXPENSE, "Food", LocalDateTime.of(2024, 2, 15, 12, 0));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /transactions?category= filters by category")
    void getAll_byCategory_returnsMatching() throws Exception {
        Account account = persistAccount("Main");
        persistTransaction(account, "30.00", TransactionType.EXPENSE, "Food", LocalDateTime.of(2024, 2, 15, 12, 0));
        persistTransaction(account, "20.00", TransactionType.EXPENSE, "Food", LocalDateTime.of(2024, 6, 1, 9, 0));
        persistTransaction(account, "50.00", TransactionType.EXPENSE, "Transport", LocalDateTime.of(2024, 6, 5, 9, 0));

        mockMvc.perform(get("/transactions").param("category", "Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", everyItem(is("Food"))));
    }

    @Test
    @DisplayName("GET /transactions?from=&to= filters by date range")
    void getAll_byDateRange_returnsMatching() throws Exception {
        Account account = persistAccount("Main");
        persistTransaction(account, "100.00", TransactionType.INCOME, "Salary", LocalDateTime.of(2024, 1, 10, 10, 0));
        persistTransaction(account, "20.00", TransactionType.EXPENSE, "Food", LocalDateTime.of(2024, 6, 1, 9, 0));
        persistTransaction(account, "50.00", TransactionType.EXPENSE, "Transport", LocalDateTime.of(2024, 6, 5, 9, 0));

        mockMvc.perform(get("/transactions")
                        .param("from", "2024-03-01T00:00:00")
                        .param("to", "2024-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /transactions/{id} -> 200")
    void getTransactionById_returnsTransaction() throws Exception {
        Account account = persistAccount("Main");
        var transaction = persistTransaction(account, "100.00", TransactionType.INCOME, "Salary", LocalDateTime.now());

        mockMvc.perform(get("/transactions/{id}", transaction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transaction.getId().intValue()))
                .andExpect(jsonPath("$.category").value("Salary"));
    }

    @Test
    @DisplayName("GET /transactions/{id} for unknown id -> 404 with error body")
    void getTransactionById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/transactions/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpectAll(errorEnvelope(HttpStatus.NOT_FOUND, "/transactions/" + NON_EXISTENT_ID))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("DELETE /transactions/{id} -> 204, reverts the account balance")
    void deleteTransaction_revertsBalance() throws Exception {
        Account account = persistAccount("Main");
        long transactionId = postTransaction(account.getId(), "100.00", "INCOME", "Salary");

        assertThat(accountRepository.findById(account.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo("100.00");

        mockMvc.perform(delete("/transactions/{id}", transactionId))
                .andExpect(status().isNoContent());

        assertThat(transactionRepository.findById(transactionId)).isEmpty();
        assertThat(accountRepository.findById(account.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("DELETE /transactions/{id} for unknown id -> 404 with error body")
    void deleteTransaction_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/transactions/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpectAll(errorEnvelope(HttpStatus.NOT_FOUND, "/transactions/" + NON_EXISTENT_ID))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }
}