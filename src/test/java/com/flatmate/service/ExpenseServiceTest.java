package com.flatmate.service;

import com.flatmate.controller.ResourceNotFoundException;
import com.flatmate.controller.ExpenseSplitRequest;
import com.flatmate.model.Expense;
import com.flatmate.model.Household;
import com.flatmate.model.User;
import com.flatmate.model.ExpenseSplit;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
        doga.setHousehold(household);
        april.setHousehold(household);

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

        verify(expenseRepository).save(
                org.mockito.ArgumentMatchers.any(Expense.class)
        );

        verify(expenseSplitRepository, org.mockito.Mockito.times(2)).save(
                org.mockito.ArgumentMatchers.any(
                        com.flatmate.model.ExpenseSplit.class
                )
        );
    }

    @Test
    void shouldReturnExpensesForExistingHousehold() {
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

        Household household = new Household(1, "Flat 21E");
        User payer = new User(1, "Doga", "doga@example.com");
        payer.setHousehold(household);
        Expense expense = new Expense(
                1,
                "Groceries",
                new BigDecimal("60.00"),
                payer,
                List.of()
        );
        List<Expense> expectedExpenses = List.of(expense);

        when(householdRepository.existsById(1)).thenReturn(true);
        when(expenseRepository.findByHouseholdId(1)).thenReturn(expectedExpenses);

        List<Expense> result = service.getExpensesByHouseholdId(1);

        assertEquals(expectedExpenses, result);
        verify(householdRepository).existsById(1);
        verify(expenseRepository).findByHouseholdId(1);
    }

    @Test
    void shouldRejectExpensesForNonexistentHousehold() {
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

        when(householdRepository.existsById(999)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getExpensesByHouseholdId(999)
        );

        assertEquals("Household not found: 999", exception.getMessage());
        verify(householdRepository).existsById(999);
        verify(expenseRepository, never()).findByHouseholdId(999);
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
        payer.setHousehold(household);
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

    @Test
    void shouldRejectPayerFromDifferentHousehold() {
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

        Household householdA = new Household(1, "Flat A");
        Household householdB = new Household(2, "Flat B");

        User payer = new User(1, "Doga", "doga@example.com");
        payer.setHousehold(householdB);

        when(userRepository.findById(1)).thenReturn(java.util.Optional.of(payer));
        when(householdRepository.findById(1)).thenReturn(java.util.Optional.of(householdA));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createExpense(
                        "Groceries",
                        new BigDecimal("10.00"),
                        1,
                        1,
                        List.of(new ExpenseSplitRequest(1, new BigDecimal("10.00")))
                )
        );

        assertEquals(
                "Payer must belong to the specified household",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectSplitUserFromDifferentHousehold() {
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

        Household householdA = new Household(1, "Flat A");
        Household householdB = new Household(2, "Flat B");

        User payer = new User(1, "Doga", "doga@example.com");
        User splitUser = new User(2, "April", "april@example.com");

        payer.setHousehold(householdA);
        splitUser.setHousehold(householdB);

        when(userRepository.findById(1)).thenReturn(java.util.Optional.of(payer));
        when(userRepository.findById(2)).thenReturn(java.util.Optional.of(splitUser));
        when(householdRepository.findById(1)).thenReturn(java.util.Optional.of(householdA));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createExpense(
                        "Groceries",
                        new BigDecimal("10.00"),
                        1,
                        1,
                        List.of(new ExpenseSplitRequest(2, new BigDecimal("10.00")))
                )
        );

        assertEquals(
                "Split user must belong to the specified household: 2",
                exception.getMessage()
        );

        verify(expenseRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Expense.class)
        );
    }

    @Test
    void shouldRejectDuplicateUsersInExpenseSplits() {
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

        Household household = new Household(1, "Flat 21E");
        User doga = new User(1, "Doga", "doga@example.com");
        doga.setHousehold(household);

        when(userRepository.findById(1)).thenReturn(java.util.Optional.of(doga));
        when(householdRepository.findById(1)).thenReturn(java.util.Optional.of(household));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createExpense(
                        "Groceries",
                        new BigDecimal("10.00"),
                        1,
                        1,
                        List.of(
                                new ExpenseSplitRequest(1, new BigDecimal("5.00")),
                                new ExpenseSplitRequest(1, new BigDecimal("5.00"))
                        )
                )
        );

        assertEquals(
                "A user can only appear once in an expense split",
                exception.getMessage()
        );

        verify(expenseRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Expense.class)
        );
    }
    @Test
    void shouldUpdateExpenseAndReplaceItsSplits() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository, userRepository, householdRepository, expenseSplitRepository
        );

        Household household = new Household(1, "Flat 21E");
        User doga = new User(1, "Doga", "doga@example.com");
        User april = new User(2, "April", "april@example.com");
        doga.setHousehold(household);
        april.setHousehold(household);

        Expense existingExpense = new Expense(
                1, "Dinner", new BigDecimal("20.00"), doga, List.of()
        );
        existingExpense.setHousehold(household);

        when(expenseRepository.findById(1)).thenReturn(java.util.Optional.of(existingExpense));
        when(userRepository.findById(1)).thenReturn(java.util.Optional.of(doga));
        when(userRepository.findById(2)).thenReturn(java.util.Optional.of(april));
        when(expenseSplitRepository.findByExpenseId(1)).thenReturn(List.of());
        when(expenseRepository.save(existingExpense)).thenReturn(existingExpense);

        Expense result = service.updateExpense(
                1,
                "Weekly groceries",
                new BigDecimal("60.00"),
                1,
                1,
                List.of(
                        new ExpenseSplitRequest(1, new BigDecimal("30.00")),
                        new ExpenseSplitRequest(2, new BigDecimal("30.00"))
                )
        );

        assertEquals("Weekly groceries", result.getDescription());
        assertEquals(0, new BigDecimal("60.00").compareTo(result.getAmount()));
        assertEquals(doga, result.getPaidBy());
        verify(expenseSplitRepository).deleteAll(List.of());
        verify(expenseSplitRepository, org.mockito.Mockito.times(2)).save(
                org.mockito.ArgumentMatchers.any(ExpenseSplit.class)
        );
    }

    @Test
    void shouldDeleteExpenseAndItsSplits() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository, userRepository, householdRepository, expenseSplitRepository
        );

        Household household = new Household(1, "Flat 21E");
        User doga = new User(1, "Doga", "doga@example.com");
        doga.setHousehold(household);
        Expense expense = new Expense(1, "Dinner", new BigDecimal("20.00"), doga, List.of());
        expense.setHousehold(household);
        List<ExpenseSplit> splits = List.of(
                new ExpenseSplit(expense, doga, new BigDecimal("20.00"))
        );

        when(expenseRepository.findById(1)).thenReturn(java.util.Optional.of(expense));
        when(expenseSplitRepository.findByExpenseId(1)).thenReturn(splits);

        service.deleteExpense(1);

        verify(expenseSplitRepository).deleteAll(splits);
        verify(expenseRepository).delete(expense);
    }

    @Test
    void shouldRejectUpdatingExpenseThatDoesNotExist() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository, userRepository, householdRepository, expenseSplitRepository
        );
        when(expenseRepository.findById(999)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateExpense(
                        999, "Dinner", new BigDecimal("20.00"), 1, 1,
                        List.of(new ExpenseSplitRequest(1, new BigDecimal("20.00")))
                )
        );

        assertEquals("Expense not found: 999", exception.getMessage());
        verify(expenseSplitRepository, never()).deleteAll(
                org.mockito.ArgumentMatchers.anyList()
        );
    }

    @Test
    void shouldRejectMovingExpenseToAnotherHousehold() {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseSplitRepository expenseSplitRepository = mock(ExpenseSplitRepository.class);

        ExpenseService service = new ExpenseService(
                expenseRepository, userRepository, householdRepository, expenseSplitRepository
        );

        Household household = new Household(1, "Flat 21E");
        User doga = new User(1, "Doga", "doga@example.com");
        doga.setHousehold(household);
        Expense expense = new Expense(1, "Dinner", new BigDecimal("20.00"), doga, List.of());
        expense.setHousehold(household);
        when(expenseRepository.findById(1)).thenReturn(java.util.Optional.of(expense));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateExpense(
                        1, "Dinner", new BigDecimal("20.00"), 1, 2,
                        List.of(new ExpenseSplitRequest(1, new BigDecimal("20.00")))
                )
        );

        assertEquals("An expense cannot be moved to another household", exception.getMessage());
        verify(expenseSplitRepository, never()).deleteAll(
                org.mockito.ArgumentMatchers.anyList()
        );
    }
}
