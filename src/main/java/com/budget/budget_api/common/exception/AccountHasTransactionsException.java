package com.budget.budget_api.common.exception;

public class AccountHasTransactionsException extends RuntimeException {
    public AccountHasTransactionsException(String message) {
        super(message);
    }
    public AccountHasTransactionsException(String message, Throwable cause){super(message, cause);}
}
