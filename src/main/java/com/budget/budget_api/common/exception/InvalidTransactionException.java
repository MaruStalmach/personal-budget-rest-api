package com.budget.budget_api.common.exception;

//400 bad request -> used for any rule violation
public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
