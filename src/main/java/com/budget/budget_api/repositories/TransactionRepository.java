package com.budget.budget_api.repositories;

import com.budget.budget_api.entities.Transaction;
import com.budget.budget_api.common.types.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId AND t.type = :type")
    BigDecimal sumAmountByAccountIdAndType(@Param("accountId") Long accountId, @Param("type") TransactionType type);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId AND t.type = :type GROUP BY t.category")
    List<Object[]> sumAmountByCategory(@Param("accountId") Long accountId, @Param("type") TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(:category IS NULL OR t.category = :category) AND " +
            "(:fromDate IS NULL OR t.transactionTime >= :fromDate) AND " +
            "(:toDate IS NULL OR t.transactionTime <= :toDate)")
    List<Transaction> findFilteredTransactions(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, @Param("category") String category);
}
