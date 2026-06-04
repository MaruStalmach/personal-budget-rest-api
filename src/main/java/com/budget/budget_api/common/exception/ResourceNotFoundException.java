package com.budget.budget_api.common.exception;

//404 not found -> used when Account or Transaction does not exist
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
