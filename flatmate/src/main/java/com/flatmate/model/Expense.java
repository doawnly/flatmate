package com.flatmate.model;
import java.math.BigDecimal;

public class Expense {
    private int id;
    private String description;
    private BigDecimal amount;
    private User paidBy;

    public Expense(int id, String description, BigDecimal amount, User paidBy) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public User getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(User paidBy) {
        this.paidBy = paidBy;
    }
}
