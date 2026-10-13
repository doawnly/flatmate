package com.flatmate.controller;

import com.flatmate.model.Household;
import com.flatmate.model.Expense;
import com.flatmate.model.User;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.repository.UserRepository;
import com.flatmate.service.ExpenseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/households")
public class HouseholdController {

    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    public HouseholdController(
            HouseholdRepository householdRepository,
            UserRepository userRepository,
            ExpenseService expenseService
    ) {
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<Household> getAllHouseholds() {
        return householdRepository.findAll();
    }

    @GetMapping("/{id}")
    public Household getHouseholdById(@PathVariable int id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found"));
    }

    @GetMapping("/{id}/users")
    public List<User> getUsersByHouseholdId(@PathVariable int id) {
        householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found"));
        return userRepository.findByHouseholdId(id);
    }

    @GetMapping("/{id}/expenses")
    public List<Expense> getExpensesByHouseholdId(@PathVariable int id) {
        return expenseService.getExpensesByHouseholdId(id);
    }

    @PostMapping
    public Household createHousehold(@RequestBody Household household) {
        return householdRepository.save(household);
    }
}
