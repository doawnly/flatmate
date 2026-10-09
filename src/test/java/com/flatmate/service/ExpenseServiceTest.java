package com.flatmate.service;

import com.flatmate.controller.ResourceNotFoundException;
import com.flatmate.controller.ExpenseSplitRequest;
import com.flatmate.model.Expense;
import com.flatmate.model.Household;
import com.flatmate.model.User;
import com.flatmate.repository.ExpenseRepository;
import com.flatmate.repository.ExpenseSplitRepository;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpenseServiceTest {

    @Test
    void shouldCreateExpenseWhenSplitsMatchAmount() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository,
                userRepository,
                householdRepository,
                expenseSplitRepository
        );

        User doga = new User(1, "Doga", "doga@example.com");
        User april = new User(2, "April", "april@example.com");
        Household household = new Household(1, "Flat 21E");

        when(userRepository.findById(1)).thenReturn(java.util.Optional.of(doga));
        when(userRepository.findById(2)).thenReturn(java.util.Optional.of(april));
        when(householdRepository.findById(1)).thenReturn(java.util.Optional.of(household));

        Expense savedExpense = new Expense(
                1,
                "Groceries",
                new BigDecimal("60.00"),
                doga,
                List.of()
        );

        when(expenseRepository.save(org.mockito.ArgumentMatchers.any(Expense.class)))
                .thenReturn(savedExpense);

        Expense result = service.createExpense(
                "Groceries",
                new BigDecimal("60.00"),
                1,
                1,
                List.of(
                        new ExpenseSplitRequest(1, new BigDecimal("30.00")),
                        new ExpenseSplitRequest(2, new BigDecimal("30.00"))
                )
        );

        assertEquals(savedExpense, result);
    }

    @Test
    void shouldRejectZeroExpenseAmount() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository,
                userRepository,
                householdRepository,
                expenseSplitRepository
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createExpense(
                        "Groceries",
                        BigDecimal.ZERO,
                        1,
                        1,
                        List.of()
                )
        );

        assertEquals("Expense amount must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldRejectNegativeExpenseAmount() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository,
                userRepository,
                householdRepository,
                expenseSplitRepository
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createExpense(
                        "Groceries",
                        new BigDecimal("-50.00"),
                        1,
                        1,
                        List.of()
                )
        );

        assertEquals("Expense amount must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldRejectBlankExpenseDescription() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository,
                userRepository,
                householdRepository,
                expenseSplitRepository
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createExpense(
                        "   ",
                        new BigDecimal("10.00"),
                        1,
                        1,
                        List.of()
                )
        );

        assertEquals("Expense description must not be blank", exception.getMessage());
    }

    @Test
    void shouldRejectEmptyExpenseSplits() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository,
                userRepository,
                householdRepository,
                expenseSplitRepository
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createExpense(
                        "Groceries",
                        new BigDecimal("10.00"),
                        1,
                        1,
                        List.of()
                )
        );

        assertEquals("At least one expense split is required", exception.getMessage());
    }

    @Test
    void shouldRejectZeroSplitAmount() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository,
                userRepository,
                householdRepository,
                expenseSplitRepository
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createExpense(
                        "Groceries",
                        new BigDecimal("10.00"),
                        1,
                        1,
                        List.of(new ExpenseSplitRequest(1, BigDecimal.ZERO))
                )
        );

        assertEquals("Split amounts must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldRejectNegativeSplitAmount() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository,
                userRepository,
                householdRepository,
                expenseSplitRepository
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createExpense(
                        "Groceries",
                        new BigDecimal("10.00"),
                        1,
                        1,
                        List.of(new ExpenseSplitRequest(1, new BigDecimal("-5.00")))
                )
        );

        assertEquals("Split amounts must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldRejectMissingPayer() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository,
                userRepository,
                householdRepository,
                expenseSplitRepository
        );

        when(userRepository.findById(99)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.createExpense(
                        "Groceries",
                        new BigDecimal("10.00"),
                        99,
                        1,
                        List.of(new ExpenseSplitRequest(1, new BigDecimal("10.00")))
                )
        );

        assertEquals("Payer not found: 99", exception.getMessage());
    }

    @Test
    void shouldRejectMissingHousehold() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository,
                userRepository,
                householdRepository,
                expenseSplitRepository
        );

        User payer = new User(1, "Doga", "doga@example.com");
        when(userRepository.findById(1)).thenReturn(java.util.Optional.of(payer));
        when(householdRepository.findById(99)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.createExpense(
                        "Groceries",
                        new BigDecimal("10.00"),
                        1,
                        99,
                        List.of(new ExpenseSplitRequest(1, new BigDecimal("10.00")))
                )
        );

        assertEquals("Household not found: 99", exception.getMessage());
    }

    @Test
    void shouldRejectMissingSplitUser() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository,
                userRepository,
                householdRepository,
                expenseSplitRepository
        );

        User payer = new User(1, "Doga", "doga@example.com");
        Household household = new Household(1, "Flat 21E");
        when(userRepository.findById(1)).thenReturn(java.util.Optional.of(payer));
        when(userRepository.findById(99)).thenReturn(java.util.Optional.empty());
        when(householdRepository.findById(1)).thenReturn(java.util.Optional.of(household));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.createExpense(
                        "Groceries",
                        new BigDecimal("10.00"),
                        1,
                        1,
                        List.of(new ExpenseSplitRequest(99, new BigDecimal("10.00")))
                )
        );

        assertEquals("Split user not found: 99", exception.getMessage());
    }
}
