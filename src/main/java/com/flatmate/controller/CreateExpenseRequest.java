package com.flatmate.controller;

import java.math.BigDecimal;

public record CreateExpenseRequest(
        String description,
        BigDecimal amount,
        int paidById,
        int householdId
) {
}