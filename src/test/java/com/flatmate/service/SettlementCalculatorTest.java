package com.flatmate.service;

import com.flatmate.model.Settlement;
import com.flatmate.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SettlementCalculatorTest {

    @Test
    void calculatesSettlements() {

        User doga = new User(1, "Doga", "doga@example.com");
        User april = new User(2, "April", "april@example.com");
        User ecren = new User(3, "Ecren", "ecren@example.com");

        List<User> users = List.of(doga, april, ecren);

        Map<Integer, BigDecimal> balances = Map.of(
                1, new BigDecimal("100.00"),
                2, new BigDecimal("-70.00"),
                3, new BigDecimal("-30.00")
        );

        SettlementCalculator calculator = new SettlementCalculator();

        List<Settlement> settlements =
                calculator.calculateSettlements(users, balances);

        assertEquals(2, settlements.size());

        assertEquals(april, settlements.get(0).getFromUser());
        assertEquals(doga, settlements.get(0).getToUser());
        assertEquals(
                new BigDecimal("70.00"),
                settlements.get(0).getAmount()
        );

        assertEquals(ecren, settlements.get(1).getFromUser());
        assertEquals(doga, settlements.get(1).getToUser());
        assertEquals(
                new BigDecimal("30.00"),
                settlements.get(1).getAmount()
        );
    }

    @Test
    void oneDebtorCanPayMultipleCreditors() {

        User doga = new User(1, "Doga", "doga@example.com");
        User april = new User(2, "April", "april@example.com");
        User ecren = new User(3, "Ecren", "ecren@example.com");

        List<User> users = List.of(doga, april, ecren);

        Map<Integer, BigDecimal> balances = Map.of(
                1, new BigDecimal("80.00"),
                2, new BigDecimal("-100.00"),
                3, new BigDecimal("20.00")
        );

        SettlementCalculator calculator = new SettlementCalculator();

        List<Settlement> settlements =
                calculator.calculateSettlements(users, balances);

        assertEquals(2, settlements.size());

        assertEquals(april, settlements.get(0).getFromUser());
        assertEquals(doga, settlements.get(0).getToUser());
        assertEquals(
                new BigDecimal("80.00"),
                settlements.get(0).getAmount()
        );

        assertEquals(april, settlements.get(1).getFromUser());
        assertEquals(ecren, settlements.get(1).getToUser());
        assertEquals(
                new BigDecimal("20.00"),
                settlements.get(1).getAmount()
        );
    }
}