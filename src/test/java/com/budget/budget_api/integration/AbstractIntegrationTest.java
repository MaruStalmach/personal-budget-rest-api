package com.budget.budget_api.integration;

import com.budget.budget_api.common.types.TransactionType;
import com.budget.budget_api.entities.Account;
import com.budget.budget_api.entities.Transaction;
import com.budget.budget_api.repositories.AccountRepository;
import com.budget.budget_api.repositories.TransactionRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    protected static final long NON_EXISTENT_ID = 999_999L;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected TransactionRepository transactionRepository;

    @BeforeEach
    void resetDatabase() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    protected Account persistAccount(String name) {
        Account account = new Account();
        account.setName(name);
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    protected Transaction persistTransaction(Account account,
                                             String amount,
                                             TransactionType type,
                                             String category,
                                             LocalDateTime time) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setTransactionTime(time);
        return transactionRepository.save(transaction);
    }

    protected String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected ResultMatcher[] errorEnvelope(HttpStatus status, String path) {
        return new ResultMatcher[]{
                jsonPath("$.status").value(status.value()),
                jsonPath("$.error").value(status.getReasonPhrase()),
                jsonPath("$.path").value(path),
                jsonPath("$.timestamp").exists(),
                jsonPath("$.message").exists()
        };
    }
}