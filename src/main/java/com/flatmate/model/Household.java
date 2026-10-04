package com.flatmate.model;
import java.util.List;

public class Household {
    private int id;
    private String name;
    private List<User> members;
    private List<Expense> expenses;

    public Household(int id, String name, List<User> members, List<Expense> expenses) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.expenses = expenses;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }
}
