package com.flatmate.controller;

import com.flatmate.model.User;
import com.flatmate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private UserRepository userRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        UserController userController = new UserController(userRepository);

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
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
}
