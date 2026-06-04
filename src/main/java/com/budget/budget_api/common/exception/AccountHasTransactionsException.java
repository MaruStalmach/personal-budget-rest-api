package com.budget.budget_api.common.exception;

//409 conflict -> used when deleting and account that has transactions
public class AccountHasTransactionsException extends RuntimeException {
    public AccountHasTransactionsException(String message) {
        super(message);
    }
}
