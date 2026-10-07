package com.flatmate.controller;

import java.math.BigDecimal;
import java.util.List;

public record CreateExpenseRequest(
        String description,
        BigDecimal amount,
        int paidById,
        int householdId,
        List<ExpenseSplitRequest> splits
) {
}