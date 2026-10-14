package com.flatmate.controller;

import com.flatmate.model.Household;
import com.flatmate.model.Expense;
import com.flatmate.model.User;
import com.flatmate.model.ExpenseCategory;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.repository.UserRepository;
import com.flatmate.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "Households", description = "Manage households, members, and household expenses")
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

    @Operation(summary = "List all households", description = "Returns all households.")
    @GetMapping
    public List<Household> getAllHouseholds() {
        return householdRepository.findAll();
    }

    @Operation(summary = "Get a household by ID", description = "Returns the household with the specified ID.")
    @GetMapping("/{id}")
    public Household getHouseholdById(@Parameter(description = "ID of the household to retrieve", example = "1") @PathVariable int id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found"));
    }

    @Operation(summary = "List household members", description = "Returns all users belonging to the specified household.")
    @GetMapping("/{id}/users")
    public List<User> getUsersByHouseholdId(@Parameter(description = "ID of the household whose members should be listed", example = "1") @PathVariable int id) {
        householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found"));
        return userRepository.findByHouseholdId(id);
    }

    @Operation(
            summary = "List household expenses",
            description = "Returns expenses for the specified household. Optional filters: category, startDate, and endDate. Results can be sorted by date or amount using sortBy, with direction set to asc or desc."
    )
    @GetMapping("/{id}/expenses")
    public List<Expense> getExpensesByHouseholdId(
            @Parameter(description = "ID of the household whose expenses should be listed", example = "1") @PathVariable int id,
            @Parameter(description = "Optional category filter, such as GROCERIES or UTILITIES") @RequestParam(required = false) ExpenseCategory category,
            @Parameter(description = "Include expenses on or after this date (YYYY-MM-DD)", example = "2026-10-01") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "Include expenses on or before this date (YYYY-MM-DD)", example = "2026-10-31") @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "Sort field: date or amount", example = "date") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction: asc or desc", example = "desc") @RequestParam(defaultValue = "desc") String direction
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

    @Operation(summary = "Create a household", description = "Creates and returns a new household.")
    @PostMapping
    public Household createHousehold(@RequestBody Household household) {
        return householdRepository.save(household);
    }
}
