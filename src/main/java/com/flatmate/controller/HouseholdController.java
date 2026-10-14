package com.flatmate.controller;

import com.flatmate.model.Household;
import com.flatmate.model.Expense;
import com.flatmate.model.User;
import com.flatmate.model.ExpenseCategory;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.repository.UserRepository;
import com.flatmate.service.ExpenseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;

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
    public List<Expense> getExpensesByHouseholdId(
            @PathVariable int id,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startDate must be on or before endDate"
            );
        }

        String sortProperty = switch (sortBy) {
            case "date" -> "date";
            case "amount" -> "amount";
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sortBy must be either 'date' or 'amount'"
            );
        };

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "direction must be either 'asc' or 'desc'"
            );
        }

        Sort sort = Sort.by(sortDirection, sortProperty);
        return expenseService.getExpensesByHouseholdId(id, category, startDate, endDate, sort);
    }

    @PostMapping
    public Household createHousehold(@RequestBody Household household) {
        return householdRepository.save(household);
    }
}
