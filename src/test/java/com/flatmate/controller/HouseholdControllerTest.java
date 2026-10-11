package com.flatmate.controller;

import com.flatmate.model.Household;
import com.flatmate.repository.HouseholdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HouseholdControllerTest {

    private HouseholdRepository householdRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        householdRepository = mock(HouseholdRepository.class);

        HouseholdController householdController =
                new HouseholdController(householdRepository);

        mockMvc = MockMvcBuilders
                .standaloneSetup(householdController)
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
}