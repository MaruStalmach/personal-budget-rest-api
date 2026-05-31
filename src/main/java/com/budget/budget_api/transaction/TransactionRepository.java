package com.budget.budget_api.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findByType(TransactionType type);
    List<Transaction> findByAccountIdAndCategory(Long accountId, String category);
}
