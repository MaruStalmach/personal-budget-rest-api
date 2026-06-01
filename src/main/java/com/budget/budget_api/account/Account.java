package com.budget.budget_api.account;

import com.budget.budget_api.common.exception.InvalidTransactionException;
import com.budget.budget_api.transaction.Transaction;
import com.budget.budget_api.transaction.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    public void applyTransaction(TransactionType type, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("transaction amount must be greater than 0");
        }
        if (type == TransactionType.INCOME) {
            this.balance = this.balance.add(amount);
        } else {
            //TODO: handle case where balance is negative (to add or not to add)
            this.balance = this.balance.subtract(amount);
        }
    }

    public void revertTransaction(TransactionType type, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("transaction amount must be greater than 0");
        }
        if (type == TransactionType.INCOME) {
            this.balance = this.balance.subtract(amount);
        } else {
            this.balance = this.balance.add(amount);
        }
    }
}
