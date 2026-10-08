package com.flatmate.model;

import java.math.BigDecimal;

public record SettlementResponse(
        String from,
        String to,
        BigDecimal amount
) {
}