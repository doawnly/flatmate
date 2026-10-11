package com.flatmate.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ExpenseSplitRequest(
        @Positive int userId,
        @NotNull @Positive BigDecimal amountOwed
) {
}