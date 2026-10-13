package com.flatmate.controller;

import com.flatmate.service.ExpenseService;

import com.flatmate.controller.CreateExpenseRequest;
import com.flatmate.controller.ExpenseController;
import com.flatmate.model.Expense;
import com.flatmate.repository.UserRepository;
import com.flatmate.model.Household;
import com.flatmate.model.User;
import com.flatmate.repository.ExpenseRepository;
import com.flatmate.repository.HouseholdRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ExpenseControllerTest {

    @Test
    void shouldCreateExpense() {

        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);

        List<ExpenseSplitRequest> splits = List.of(
                new ExpenseSplitRequest(1, new BigDecimal("20.00"))
        );

        ExpenseController controller = new ExpenseController(
                null,
                expenseService,
                userRepository,
                householdRepository
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
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseController controller = new ExpenseController(null, expenseService, userRepository, householdRepository);
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

    @Test
    void shouldRejectNonPositiveExpenseAmountBeforeCallingService() throws Exception {
        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseController controller = new ExpenseController(null, expenseService, userRepository, householdRepository);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String invalidRequest = """
                {
                  "description": "Groceries",
                  "amount": -10.00,
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

    @Test
    void shouldRejectNonPositiveSplitAmountBeforeCallingService() throws Exception {
        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseController controller = new ExpenseController(null, expenseService, userRepository, householdRepository);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String invalidRequest = """
            {
              "description": "Groceries",
              "amount": 10.00,
              "paidById": 1,
              "householdId": 1,
              "splits": [
                {"userId": 1, "amountOwed": -10.00}
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

    @Test
    void shouldReturnNotFoundForBalancesOfNonexistentHousehold() throws Exception {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        when(householdRepository.existsById(999)).thenReturn(false);

        ExpenseController controller = new ExpenseController(
                expenseRepository, expenseService, userRepository, householdRepository);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/expenses/households/999/balances"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(userRepository, expenseRepository);
    }

    @Test
    void shouldReturnNotFoundForSettlementsOfNonexistentHousehold() throws Exception {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        when(householdRepository.existsById(999)).thenReturn(false);

        ExpenseController controller = new ExpenseController(
                expenseRepository, expenseService, userRepository, householdRepository);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/expenses/households/999/settlements"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(userRepository, expenseRepository);
    }

    @Test
    void shouldIncludeOnlyUsersFromRequestedHouseholdInBalances() throws Exception {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        when(householdRepository.existsById(1)).thenReturn(true);

        Household householdOne = new Household();
        householdOne.setId(1);
        Household householdTwo = new Household();
        householdTwo.setId(2);

        User doga = new User();
        doga.setId(1);
        doga.setName("Doga");
        doga.setHousehold(householdOne);

        User april = new User();
        april.setId(2);
        april.setName("April");
        april.setHousehold(householdTwo);

        when(userRepository.findAll()).thenReturn(List.of(doga, april));
        when(expenseRepository.findByHouseholdId(1)).thenReturn(List.of());

        ExpenseController controller = new ExpenseController(
                expenseRepository, expenseService, userRepository, householdRepository);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/expenses/households/1/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Doga").value(0))
                .andExpect(jsonPath("$.April").doesNotExist());

        verify(expenseRepository).findByHouseholdId(1);
    }
}