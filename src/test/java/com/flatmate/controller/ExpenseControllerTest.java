package com.flatmate.controller;

import com.flatmate.service.ExpenseService;

import com.flatmate.controller.CreateExpenseRequest;
import com.flatmate.controller.ExpenseController;
import com.flatmate.model.Expense;
import com.flatmate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

    @Test
    void shouldRejectInvalidCreateExpenseRequestBeforeCallingService() throws Exception {
        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        ExpenseController controller = new ExpenseController(null, expenseService, userRepository);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String invalidRequest = """
                {
                  "description": "   ",
                  "amount": 10.00,
                  "paidById": 1,
                  "householdId": 1,
                  "splits": [
                    {"userId": 1, "amountOwed": 10.00}
                  ]
                }
                """;

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verifyNoInteractions(expenseService);
    }
}