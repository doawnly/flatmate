package com.flatmate.model;

import java.math.BigDecimal;

public class Settlement {

    private User fromUser;
    private User toUser;
    private BigDecimal amount;

    public Settlement(User fromUser, User toUser, BigDecimal amount) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
    }

    public User getFromUser() {
        return fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}