package com.flatmate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.flatmate.model.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateExpenseRequest(
        @NotBlank String description,
        @NotNull @Positive BigDecimal amount,
        @Positive int paidById,
        @Positive int householdId,
        @NotEmpty List<@Valid ExpenseSplitRequest> splits,
        ExpenseCategory category,
        LocalDate date
) {
    public CreateExpenseRequest {
        if (category == null) {
            category = ExpenseCategory.OTHER;
        }
        if (date == null) {
            date = LocalDate.now();
        }
    }

    public CreateExpenseRequest(
            String description,
            BigDecimal amount,
            int paidById,
            int householdId,
            List<ExpenseSplitRequest> splits
    ) {
        this(description, amount, paidById, householdId, splits, ExpenseCategory.OTHER, LocalDate.now());
    }
}