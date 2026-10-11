package com.flatmate.controller;

import com.flatmate.model.User;
import com.flatmate.repository.UserRepository;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.model.Household;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private UserRepository userRepository;
    private HouseholdRepository householdRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        householdRepository = mock(HouseholdRepository.class);
        UserController userController = new UserController(userRepository, householdRepository);

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnUserWhenIdExists() throws Exception {
        User user = new User();
        user.setId(1);
        user.setName("Dowi");
        user.setEmail("dowi@example.com");

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Dowi"))
                .andExpect(jsonPath("$.email").value("dowi@example.com"));

        verify(userRepository).findById(1);
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        when(userRepository.findById(999))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userRepository).findById(999);
    }

    @Test
    void shouldCreateUserWhenHouseholdExists() throws Exception {
        Household household = new Household();
        household.setId(1);

        when(householdRepository.findById(1))
                .thenReturn(Optional.of(household));

        User savedUser = new User();
        savedUser.setId(2);
        savedUser.setName("Dowi");
        savedUser.setEmail("dowi@example.com");
        savedUser.setHousehold(household);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Dowi",
                                  "email": "dowi@example.com",
                                  "householdId": 1
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Dowi"))
                .andExpect(jsonPath("$.email").value("dowi@example.com"));

        verify(householdRepository).findById(1);
        verify(userRepository).save(any(User.class));
    }
    @Test
    void shouldReturn404WhenCreatingUserForMissingHousehold() throws Exception {
        when(householdRepository.findById(999))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Dowi",
                                  "email": "dowi@example.com",
                                  "householdId": 999
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(householdRepository).findById(999);
        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void shouldReturn400WhenCreatingUserWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Dowi",
                                  "email": "not-an-email",
                                  "householdId": 1
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("email: Email must be valid"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldReturn400WhenCreatingUserWithBlankName() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "email": "dowi@example.com",
                                  "householdId": 1
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void shouldReturn400WhenCreatingUserWithoutHouseholdId() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Dowi",
                                  "email": "dowi@example.com"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).save(any(User.class));
    }
}
