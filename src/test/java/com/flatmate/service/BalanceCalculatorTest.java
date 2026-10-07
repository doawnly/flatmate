package com.flatmate.service;

import com.flatmate.model.Expense;
import com.flatmate.model.ExpenseSplit;
import java.math.BigDecimal;
import java.util.List;
import com.flatmate.model.User;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BalanceCalculatorTest {

    @Test
    void calculateBalances_shouldReturnCorrectBalances() {
        User doga = new User(1, "Doga", "doga@example.com");
        User april = new User(2, "April", "april@example.com");
        User ecren = new User(3, "Ecren", "ecren@example.com");

        Expense expense = new Expense(1, "Dinner", new BigDecimal("120.00"), doga,
                List.of(
                    new ExpenseSplit(1, doga, new BigDecimal("40.00")),
                    new ExpenseSplit(2, april, new BigDecimal("40.00")),
                    new ExpenseSplit(3, ecren, new BigDecimal("40.00"))
                )
        );

        BalanceCalculator calculator = new BalanceCalculator();
        Map<Integer, BigDecimal> balances = calculator.calculateBalances(

                List.of(doga, april, ecren),
                List.of(expense)
        );
        assertEquals(new BigDecimal("80.00"), balances.get(doga.getId()));
        assertEquals(new BigDecimal("-40.00"), balances.get(april.getId()));
        assertEquals(new BigDecimal("-40.00"), balances.get(ecren.getId()));
    }

    @Test
    void calculateBalances_shouldHandleMultipleExpenses() {
        User doga = new User(1, "Doga", "doga@example.com");
        User april = new User(2, "April", "april@example.com");
        User ecren = new User(3, "Ecren", "ecren@example.com");

        Expense groceries = new Expense(1, "Groceries", new BigDecimal("60.00"), doga,
                List.of(
                    new ExpenseSplit(1, doga, new BigDecimal("20.00")),
                    new ExpenseSplit(2, april, new BigDecimal("20.00")),
                    new ExpenseSplit(3, ecren, new BigDecimal("20.00"))
                )
        );

        Expense utilities = new Expense(2, "Utilities", new BigDecimal("30.00"), april,
                List.of(
                    new ExpenseSplit(4, doga, new BigDecimal("10.00")),
                    new ExpenseSplit(5, april, new BigDecimal("10.00")),
                    new ExpenseSplit(6, ecren, new BigDecimal("10.00"))
                )
        );

        BalanceCalculator calculator = new BalanceCalculator();
        Map<Integer, BigDecimal> balances = calculator.calculateBalances(
                List.of(doga, april, ecren),
                List.of(groceries, utilities)
        );

        assertEquals(new BigDecimal("30.00"), balances.get(doga.getId()));
        assertEquals(new BigDecimal("0.00"), balances.get(april.getId()));
        assertEquals(new BigDecimal("-30.00"), balances.get(ecren.getId()));
    }
}