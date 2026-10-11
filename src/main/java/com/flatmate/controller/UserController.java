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

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;

    public UserController(UserRepository userRepository, HouseholdRepository householdRepository) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @PostMapping
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        Household household = householdRepository.findById(request.getHouseholdId())
                .orElseThrow(() -> new ResourceNotFoundException("Household not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setHousehold(household);

        return userRepository.save(user);
    }
}
