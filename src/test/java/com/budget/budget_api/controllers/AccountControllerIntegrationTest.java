package com.budget.budget_api.controllers;

import com.budget.budget_api.common.types.TransactionType;
import com.budget.budget_api.entities.Account;
import com.budget.budget_api.integration.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /accounts -> 201, persists account with zero balance")
    void createAccount_returns201() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Main account"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Main account"))
                .andExpect(jsonPath("$.balance").exists());

        assertThat(accountRepository.findAll()).hasSize(1);
        assertThat(accountRepository.findAll().get(0).getBalance()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("POST /accounts with blank name -> 400 with validation error body")
    void createAccount_blankName_returns400() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("name", "   "))))
                .andExpect(status().isBadRequest())
                .andExpectAll(errorEnvelope(HttpStatus.BAD_REQUEST, "/accounts"))
                .andExpect(jsonPath("$.message", containsString("validation")));

        assertThat(accountRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /accounts with missing name -> 400 with validation error body")
    void createAccount_missingName_returns400() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of())))
                .andExpect(status().isBadRequest())
                .andExpectAll(errorEnvelope(HttpStatus.BAD_REQUEST, "/accounts"))
                .andExpect(jsonPath("$.message", containsString("validation")));
    }

    @Test
    @DisplayName("GET /accounts -> 200, returns all accounts")
    void getAllAccounts_returnsList() throws Exception {
        persistAccount("Main");
        persistAccount("Savings");

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /accounts/{id} -> 200, returns the account with current balance")
    void getAccountById_returnsAccount() throws Exception {
        Account account = persistAccount("Savings");

        mockMvc.perform(get("/accounts/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId().intValue()))
                .andExpect(jsonPath("$.name").value("Savings"));
    }

    @Test
    @DisplayName("GET /accounts/{id} for unknown id -> 404 with error body")
    void getAccountById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/accounts/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpectAll(errorEnvelope(HttpStatus.NOT_FOUND, "/accounts/" + NON_EXISTENT_ID))
                .andExpect(jsonPath("$.message", containsString("cannot be found")));
    }

    @Test
    @DisplayName("DELETE /accounts/{id} with no transactions -> 204, removes the account")
    void deleteAccount_returns204() throws Exception {
        Account account = persistAccount("Temporary");

        mockMvc.perform(delete("/accounts/{id}", account.getId()))
                .andExpect(status().isNoContent());

        assertThat(accountRepository.findById(account.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /accounts/{id} for unknown id -> 404 with error body")
    void deleteAccount_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/accounts/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpectAll(errorEnvelope(HttpStatus.NOT_FOUND, "/accounts/" + NON_EXISTENT_ID))
                .andExpect(jsonPath("$.message", containsString("cannot be found")));
    }

    @Test
    @DisplayName("DELETE /accounts/{id} that still has transactions -> 409, keeps the account")
    void deleteAccount_withTransactions_returns409() throws Exception {
        Account account = persistAccount("Active");
        persistTransaction(account, "100.00", TransactionType.INCOME, "Salary", LocalDateTime.now());

        mockMvc.perform(delete("/accounts/{id}", account.getId()))
                .andExpect(status().isConflict())
                .andExpectAll(errorEnvelope(HttpStatus.CONFLICT, "/accounts/" + account.getId()))
                .andExpect(jsonPath("$.message", containsString("cannot delete")));

        assertThat(accountRepository.findById(account.getId())).isPresent();
    }
}