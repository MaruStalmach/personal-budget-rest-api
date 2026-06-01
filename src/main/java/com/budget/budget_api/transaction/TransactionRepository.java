package com.budget.budget_api.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId AND t.type = :type")
    BigDecimal sumAmountByAccountIdAndType(@Param("accountId") Long accountId, @Param("type") TransactionType type);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId AND t.type = :type GROUP BY t.category")
    List<Object[]> sumAmountByCategory(@Param("accountId") Long accountId, @Param("type") TransactionType type);
}
