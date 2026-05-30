package com.budget.budget_api.account.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountRequest(
        @NotBlank(message = "account name cannot be empty")
        String name
) {
}
