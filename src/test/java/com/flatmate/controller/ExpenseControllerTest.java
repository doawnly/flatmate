package com.flatmate.controller;

import com.flatmate.model.ExpenseSplit;
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
import com.flatmate.model.ExpenseCategory;
import java.time.LocalDate;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

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
                eq("Groceries"),
                eq(new BigDecimal("60.00")),
                eq(1),
                eq(1),
                eq(splits),
                eq(ExpenseCategory.OTHER),
                any(LocalDate.class)
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
                eq("Groceries"),
                eq(new BigDecimal("60.00")),
                eq(1),
                eq(1),
                eq(splits),
                eq(ExpenseCategory.OTHER),
                any(LocalDate.class)
        );
    }

    @Test
    void shouldCreateExpenseWithCustomCategoryAndDate() throws Exception {
        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);
        ExpenseController controller = new ExpenseController(null, expenseService, userRepository, householdRepository);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        LocalDate expenseDate = LocalDate.of(2026, 10, 1);
        Expense expectedExpense = new Expense();
        expectedExpense.setCategory(ExpenseCategory.GROCERIES);
        expectedExpense.setDate(expenseDate);

        when(expenseService.createExpense(
                eq("Weekly groceries"),
                eq(new BigDecimal("60.00")),
                eq(1),
                eq(1),
                anyList(),
                eq(ExpenseCategory.GROCERIES),
                eq(expenseDate)
        )).thenReturn(expectedExpense);

        String requestBody = """
            {
              "description": "Weekly groceries",
              "amount": 60.00,
              "paidById": 1,
              "householdId": 1,
              "category": "GROCERIES",
              "date": "2026-10-01",
              "splits": [
                {"userId": 1, "amountOwed": 60.00}
              ]
            }
            """;

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(expenseService).createExpense(
                eq("Weekly groceries"),
                eq(new BigDecimal("60.00")),
                eq(1),
                eq(1),
                anyList(),
                eq(ExpenseCategory.GROCERIES),
                eq(expenseDate)
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

    @Test
    void shouldReturnSettlementsForHousehold() throws Exception {
        ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);

        when(householdRepository.existsById(1)).thenReturn(true);

        Household household = new Household();
        household.setId(1);

        User doga = new User();
        doga.setId(1);
        doga.setName("Doga");
        doga.setHousehold(household);

        User april = new User();
        april.setId(2);
        april.setName("April");
        april.setHousehold(household);

        when(userRepository.findAll()).thenReturn(List.of(doga, april));

        Expense expense = new Expense();
        expense.setId(1);
        expense.setDescription("Dinner");
        expense.setAmount(new BigDecimal("100.00"));
        expense.setPaidBy(doga);
        expense.setHousehold(household);

        ExpenseSplit aprilSplit = new ExpenseSplit();
        aprilSplit.setUser(april);
        aprilSplit.setAmountOwed(new BigDecimal("50.00"));
        aprilSplit.setExpense(expense);

        ExpenseSplit dogaSplit = new ExpenseSplit();
        dogaSplit.setUser(doga);
        dogaSplit.setAmountOwed(new BigDecimal("50.00"));
        dogaSplit.setExpense(expense);

        expense.setSplits(List.of(aprilSplit, dogaSplit));

        when(expenseRepository.findByHouseholdId(1))
                .thenReturn(List.of(expense));

        ExpenseController controller = new ExpenseController(
                expenseRepository,
                expenseService,
                userRepository,
                householdRepository
        );

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/expenses/households/1/settlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].from").value("April"))
                .andExpect(jsonPath("$[0].to").value("Doga"))
                .andExpect(jsonPath("$[0].amount").value(50.0));

        verify(expenseRepository).findByHouseholdId(1);
    }

    @Test
    void shouldUpdateExpenseThroughPutEndpoint() throws Exception {
        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);

        ExpenseController controller = new ExpenseController(
                null, expenseService, userRepository, householdRepository
        );

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Expense expectedExpense = new Expense();

        when(expenseService.updateExpense(
                eq(1),
                eq("Dinner"),
                eq(new BigDecimal("40.00")),
                eq(1),
                eq(1),
                anyList(),
                eq(ExpenseCategory.OTHER),
                any(LocalDate.class)
        )).thenReturn(expectedExpense);

        String requestBody = """
            {
              "description": "Dinner",
              "amount": 40.00,
              "paidById": 1,
              "householdId": 1,
              "splits": [
                {"userId": 1, "amountOwed": 20.00},
                {"userId": 2, "amountOwed": 20.00}
              ]
            }
            """;

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(expenseService).updateExpense(
                eq(1),
                eq("Dinner"),
                eq(new BigDecimal("40.00")),
                eq(1),
                eq(1),
                anyList(),
                eq(ExpenseCategory.OTHER),
                any(LocalDate.class)
        );
    }

    @Test
    void shouldDeleteExpenseThroughDeleteEndpoint() throws Exception {
        ExpenseService expenseService = mock(ExpenseService.class);
        UserRepository userRepository = mock(UserRepository.class);
        HouseholdRepository householdRepository = mock(HouseholdRepository.class);

        ExpenseController controller = new ExpenseController(
                null, expenseService, userRepository, householdRepository
        );

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());

        verify(expenseService).deleteExpense(1);
    }
}