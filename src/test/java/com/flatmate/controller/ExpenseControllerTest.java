package com.flatmate.controller;

import com.flatmate.service.ExpenseService;

import com.flatmate.controller.CreateExpenseRequest;
import com.flatmate.controller.ExpenseController;
import com.flatmate.model.Expense;
import com.flatmate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExpenseControllerTest {

    @Test
    void shouldCreateExpense() {

        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);

        List<ExpenseSplitRequest> splits = List.of(
                new ExpenseSplitRequest(1, new BigDecimal("20.00"))
        );

        ExpenseController controller = new ExpenseController(
                null,
                expenseService,
                userRepository
        );

        Expense expectedExpense = new Expense();

        when(expenseService.createExpense(
                "Groceries",
                new BigDecimal("60.00"),
                1,
                1,
                splits
        )).thenReturn(expectedExpense);

        CreateExpenseRequest request = new CreateExpenseRequest(
                "Groceries",
                new BigDecimal("60.00"),
                1,
                1,
                splits
        );

        Expense result = controller.createExpense(request);

        assertEquals(expectedExpense, result);

        verify(expenseService).createExpense(
                "Groceries",
                new BigDecimal("60.00"),
                1,
                1,
                splits
        );
    }
}