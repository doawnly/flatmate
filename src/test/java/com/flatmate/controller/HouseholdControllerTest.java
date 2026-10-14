package com.flatmate.controller;

import com.flatmate.model.Household;
import com.flatmate.model.User;
import com.flatmate.model.ExpenseCategory;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.repository.UserRepository;
import com.flatmate.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HouseholdControllerTest {

    private HouseholdRepository householdRepository;
    private UserRepository userRepository;
    private ExpenseService expenseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        householdRepository = mock(HouseholdRepository.class);
        userRepository = mock(UserRepository.class);
        expenseService = mock(ExpenseService.class);

        HouseholdController householdController =
                new HouseholdController(householdRepository, userRepository, expenseService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(householdController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnAllHouseholds() throws Exception {
        Household household = new Household(1, "Flat A");

        when(householdRepository.findAll())
                .thenReturn(List.of(household));

        mockMvc.perform(get("/api/households")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Flat A"));

        verify(householdRepository).findAll();
    }

    @Test
    void shouldReturnHouseholdWhenIdExists() throws Exception {
        Household household = new Household(1, "Flat A");

        when(householdRepository.findById(1))
                .thenReturn(Optional.of(household));

        mockMvc.perform(get("/api/households/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Flat A"));

        verify(householdRepository).findById(1);
    }

    @Test
    void shouldReturn404WhenHouseholdDoesNotExist() throws Exception {
        when(householdRepository.findById(999))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/households/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(householdRepository).findById(999);
    }

    @Test
    void shouldCreateHousehold() throws Exception {
        Household savedHousehold = new Household(1, "Flat A");

        when(householdRepository.save(any(Household.class)))
                .thenReturn(savedHousehold);

        mockMvc.perform(post("/api/households")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Flat A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Flat A"));

        verify(householdRepository).save(any(Household.class));
    }

    @Test
    void shouldReturnUsersForHousehold() throws Exception {
        Household household = new Household(1, "Flat A");
        User user = new User(1, "Doga", "doga@example.com");

        when(householdRepository.findById(1))
                .thenReturn(Optional.of(household));
        when(userRepository.findByHouseholdId(1))
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/households/1/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Doga"))
                .andExpect(jsonPath("$[0].email").value("doga@example.com"));

        verify(userRepository).findByHouseholdId(1);
    }

    @Test
    void shouldReturn404WhenListingUsersForMissingHousehold() throws Exception {
        when(householdRepository.findById(999))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/households/999/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userRepository, never()).findByHouseholdId(999);
    }

    @Test
    void shouldReturnExpensesForHousehold() throws Exception {
        when(expenseService.getExpensesByHouseholdId(1))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/households/1/expenses")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(expenseService).getExpensesByHouseholdId(
                1,
                null,
                null,
                null,
                Sort.by(Sort.Direction.DESC, "date")
        );
    }

    @Test
    void shouldPassCategoryDateFiltersAndSortingToExpenseService() throws Exception {
        mockMvc.perform(get("/api/households/1/expenses")
                        .param("category", "GROCERIES")
                        .param("startDate", "2026-10-01")
                        .param("endDate", "2026-10-13")
                        .param("sortBy", "amount")
                        .param("direction", "asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(expenseService).getExpensesByHouseholdId(
                1,
                ExpenseCategory.GROCERIES,
                LocalDate.parse("2026-10-01"),
                LocalDate.parse("2026-10-13"),
                Sort.by(Sort.Direction.ASC, "amount")
        );
    }

    @Test
    void shouldReturn400WhenStartDateIsAfterEndDate() throws Exception {
        mockMvc.perform(get("/api/households/1/expenses")
                        .param("startDate", "2026-10-13")
                        .param("endDate", "2026-10-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }

    @Test
    void shouldReturn400WhenSortByIsUnsupported() throws Exception {
        mockMvc.perform(get("/api/households/1/expenses")
                        .param("sortBy", "description")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }

    @Test
    void shouldReturn400WhenSortDirectionIsUnsupported() throws Exception {
        mockMvc.perform(get("/api/households/1/expenses")
                        .param("direction", "sideways")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }
}