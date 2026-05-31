package com.budget.budget_api.summary;

import com.budget.budget_api.account.AccountRepository;
import com.budget.budget_api.account.AccountService;
import com.budget.budget_api.summary.dto.SummaryResponse;
import com.budget.budget_api.transaction.Transaction;
import com.budget.budget_api.transaction.TransactionRepository;
import com.budget.budget_api.transaction.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;


    public SummaryService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;

    }

    public SummaryResponse getSummary(Long accountId) {
        //TODO: add error handling for missing acc
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        BigDecimal totalIncome = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpences = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> expensesByCategory = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory), Collectors.reducing((BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        return new SummaryResponse(totalIncome, totalExpences, expensesByCategory);
    }
}
