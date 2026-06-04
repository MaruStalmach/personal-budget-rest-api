package com.budget.budget_api.services;

import com.budget.budget_api.repositories.AccountRepository;
import com.budget.budget_api.common.exception.ResourceNotFoundException;
import com.budget.budget_api.dtos.responses.SummaryResponse;
import com.budget.budget_api.repositories.TransactionRepository;
import com.budget.budget_api.common.types.TransactionType;
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

        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("account with a given id cannot be found");
        }

        BigDecimal totalIncome = getSafeSum(accountId, TransactionType.INCOME);
        BigDecimal totalExpense = getSafeSum(accountId, TransactionType.EXPENSE);

        Map<String, BigDecimal> expensesByCategory = getPerCategoryBreakdown(accountId, TransactionType.EXPENSE);

        return new SummaryResponse(totalIncome, totalExpense, expensesByCategory);
    }

    private BigDecimal getSafeSum(Long accountId, TransactionType transactionType) {
        BigDecimal sum = transactionRepository.sumAmountByAccountIdAndType(accountId, transactionType);

        if (sum != null) {
            return sum;
        }
        return BigDecimal.ZERO;
    }

    private Map<String, BigDecimal> getPerCategoryBreakdown(Long accountId, TransactionType transactionType) {
        List<Object[]> result = transactionRepository.sumAmountByCategory(accountId, transactionType);
        return result.stream().collect(
                Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                )
        );
    }


}
