package com.flatmate.controller;

import com.flatmate.model.User;
import com.flatmate.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import jakarta.validation.Valid;

import com.flatmate.dto.CreateUserRequest;
import com.flatmate.model.Household;
import com.flatmate.repository.HouseholdRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "Users", description = "Manage flatmates and their household membership")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;

    public UserController(UserRepository userRepository, HouseholdRepository householdRepository) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
    }

    @Operation(summary = "List all users", description = "Returns all users registered in the application.")
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Operation(summary = "Get a user by ID", description = "Returns the user with the specified ID, or a not-found error if no such user exists.")
    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Operation(summary = "Create a user", description = "Creates a user in an existing household. The email must be unique, and the request must include valid user details and a household ID.")
    @PostMapping
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Email is already registered");
        }

        Household household = householdRepository.findById(request.getHouseholdId())
                .orElseThrow(() -> new ResourceNotFoundException("Household not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setHousehold(household);

        return userRepository.save(user);
    }
}
