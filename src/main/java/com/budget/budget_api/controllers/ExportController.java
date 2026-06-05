package com.budget.budget_api.controllers;

import com.budget.budget_api.dtos.responses.TransactionResponse;

import com.budget.budget_api.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/accounts")
public class ExportController {

    private final TransactionService transactionService;

    public ExportController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping(value = "/{id}/transactions/export", produces = "text/csv")
    public ResponseEntity<String> exportTransactionsForAccount(@PathVariable("id") Long accountId) {
        List<TransactionResponse> transactionList = transactionService.getTransactionsByAccountId(accountId);


        String[] csvHeader = {"id", "amount", "type", "category", "description", "transactionTime"};

        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", csvHeader)).append("\n");

        for (TransactionResponse t : transactionList) {
            String[] row = {
                    String.valueOf(t.id()),
                    String.valueOf(t.amount()),
                    String.valueOf(t.type()),
                    t.category(),
                    t.description(),
                    String.valueOf(t.transactionTime())
            };
            csv.append(Arrays.stream(row).map(this::escapeCsv).collect(Collectors.joining(","))).append("\n");
        }

        String filename = "account-" + accountId + "-transactions.csv";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(csv.toString());

    }

    private String escapeCsv(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }


}