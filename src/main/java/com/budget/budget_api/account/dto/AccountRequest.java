package com.budget.budget_api.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountRequest(
        @NotBlank(message = "account name cannot be empty")
        @Size(max = 50, message = "account name cannot be longer than 50 characters")
        String name
) {
}
