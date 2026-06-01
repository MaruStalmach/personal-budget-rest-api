package com.budget.budget_api.common.exception;

//409 conflict -> used when trying to create an account with duplicate name
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
