package com.flatmate.service;

import com.flatmate.model.Settlement;
import com.flatmate.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        List<Settlement> settlements = calculator.calculateSettlements(users, balances);

        assertEquals(2, settlements.size());
        assertSettlementExists(settlements, april, doga, "70.00");
        assertSettlementExists(settlements, ecren, doga, "30.00");
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
        List<Settlement> settlements = calculator.calculateSettlements(users, balances);

        assertEquals(2, settlements.size());
        assertSettlementExists(settlements, april, doga, "80.00");
        assertSettlementExists(settlements, april, ecren, "20.00");
    }

    @Test
    void shouldReturnNoSettlementsWhenEveryoneIsSettled() {
        User doga = new User(1, "Doga", "doga@example.com");
        User april = new User(2, "April", "april@example.com");

        Map<Integer, BigDecimal> balances = Map.of(
                1, BigDecimal.ZERO,
                2, BigDecimal.ZERO
        );

        SettlementCalculator calculator = new SettlementCalculator();
        List<Settlement> settlements = calculator.calculateSettlements(
                List.of(doga, april), balances
        );

        assertTrue(settlements.isEmpty());
    }

    private void assertSettlementExists(
            List<Settlement> settlements,
            User fromUser,
            User toUser,
            String expectedAmount) {

        boolean found = settlements.stream().anyMatch(settlement ->
                settlement.getFromUser().equals(fromUser)
                        && settlement.getToUser().equals(toUser)
                        && settlement.getAmount().compareTo(new BigDecimal(expectedAmount)) == 0
        );

        assertTrue(found, () -> "Expected settlement: " + fromUser.getName()
                + " pays " + toUser.getName() + " £" + expectedAmount);
    }

    @Test
    void handlesMultipleDebtorsAndCreditorsWithDifferentAmounts() {
        User doga = new User(1, "Doga", "doga@example.com");
        User april = new User(2, "April", "april@example.com");
        User ecren = new User(3, "Ecren", "ecren@example.com");
        User alex = new User(4, "Alex", "alex@example.com");

        List<User> users = List.of(doga, april, ecren, alex);

        Map<Integer, BigDecimal> balances = Map.of(
                1, new BigDecimal("50.00"),
                2, new BigDecimal("30.00"),
                3, new BigDecimal("-60.00"),
                4, new BigDecimal("-20.00")
        );

        SettlementCalculator calculator = new SettlementCalculator();
        List<Settlement> settlements =
                calculator.calculateSettlements(users, balances);

        assertEquals(3, settlements.size());

        assertSettlementExists(settlements, ecren, doga, "50.00");
        assertSettlementExists(settlements, ecren, april, "10.00");
        assertSettlementExists(settlements, alex, april, "20.00");
    }
}