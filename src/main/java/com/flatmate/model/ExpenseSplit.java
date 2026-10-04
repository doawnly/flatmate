package com.flatmate.model;
import java.math.BigDecimal;

public class ExpenseSplit {
    private int id;
    private User user;
    private BigDecimal amountOwed;

    public ExpenseSplit(int id, User user, BigDecimal amountOwed) {
        this.id = id;
        this.user = user;
        this.amountOwed = amountOwed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getAmountOwed() {
        return amountOwed;
    }

    public void setAmountOwed(BigDecimal amountOwed) {
        this.amountOwed = amountOwed;
    }
}
