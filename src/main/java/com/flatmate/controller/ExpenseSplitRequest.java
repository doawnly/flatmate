package com.flatmate.controller;

import java.math.BigDecimal;

public record ExpenseSplitRequest(
        int userId,
        BigDecimal amountOwed
) {
}