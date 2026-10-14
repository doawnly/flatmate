package com.flatmate.controller;

import com.flatmate.model.Expense;
import com.flatmate.repository.ExpenseRepository;
import com.flatmate.service.ExpenseService;
import com.flatmate.model.User;
import com.flatmate.repository.UserRepository;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.service.BalanceCalculator;
import com.flatmate.service.SettlementCalculator;
import com.flatmate.model.SettlementResponse;
import java.math.BigDecimal;
import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.ResponseEntity;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Tag(name = "Expenses", description = "Create, view, update, and delete expenses, and calculate household balances and settlements.")
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;

    public ExpenseController(
            ExpenseRepository expenseRepository,
            ExpenseService expenseService,
            UserRepository userRepository,
            HouseholdRepository householdRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
    }

    @Operation(summary = "List all expenses", description = "Returns all expenses with payer, household, category, and date information.")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(expense -> new ExpenseResponse(
                        expense.getId(),
                        expense.getDescription(),
                        expense.getAmount(),
                        expense.getPaidBy().getId(),
                        expense.getPaidBy().getName(),
                        expense.getHousehold().getId(),
                        expense.getCategory(),
                        expense.getDate()
                ))
                .toList();
    }

    public record ExpenseResponse(
            int id,
            String description,
            BigDecimal amount,
            int paidById,
            String paidByName,
            int householdId,
            com.flatmate.model.ExpenseCategory category,
            java.time.LocalDate date
    ) {}

    @Operation(summary = "Get default household balances", description = "Returns the calculated balances for household 1.")
    @ApiResponse(responseCode = "200", description = "Balances calculated successfully")
    @GetMapping("/balances")
    public Map<String, BigDecimal> getBalances() {
        return getBalancesForHousehold(1);
    }

    @Operation(summary = "Get default household settlements", description = "Returns suggested repayments for household 1.")
    @ApiResponse(responseCode = "200", description = "Settlements calculated successfully")
    @GetMapping("/settlements")
    public List<SettlementResponse> getSettlements() {
        return getSettlementsForHousehold(1);
    }

    @Operation(summary = "Get household balances", description = "Calculates how much each user in the specified household owes or is owed.")
    @ApiResponse(responseCode = "200", description = "Balances calculated successfully")
    @ApiResponse(responseCode = "404", description = "Household not found")
    @GetMapping("/households/{householdId}/balances")
    public Map<String, BigDecimal> getBalancesByHousehold(@Parameter(description = "ID of the household to calculate balances for", example = "1") @PathVariable int householdId) {
        return getBalancesForHousehold(householdId);
    }

    @Operation(summary = "Get household settlements", description = "Returns suggested repayments that settle the specified household's balances.")
    @ApiResponse(responseCode = "200", description = "Settlements calculated successfully")
    @ApiResponse(responseCode = "404", description = "Household not found")
    @GetMapping("/households/{householdId}/settlements")
    public List<SettlementResponse> getSettlementsByHousehold(@Parameter(description = "ID of the household to calculate settlements for", example = "1") @PathVariable int householdId) {
        return getSettlementsForHousehold(householdId);
    }

    private Map<String, BigDecimal> getBalancesForHousehold(int householdId) {
        validateHouseholdExists(householdId);
        List<User> users = getUsersForHousehold(householdId);
        List<Expense> expenses = getExpensesForHousehold(householdId);

        BalanceCalculator calculator = new BalanceCalculator();
        Map<Integer, BigDecimal> balances = calculator.calculateBalances(users, expenses);

        Map<String, BigDecimal> namedBalances = new java.util.HashMap<>();
        for (User user : users) {
            namedBalances.put(user.getName(), balances.get(user.getId()));
        }

        return namedBalances;
    }

    private List<SettlementResponse> getSettlementsForHousehold(int householdId) {
        validateHouseholdExists(householdId);
        List<User> users = getUsersForHousehold(householdId);
        List<Expense> expenses = getExpensesForHousehold(householdId);

        BalanceCalculator balanceCalculator = new BalanceCalculator();
        Map<Integer, BigDecimal> balances = balanceCalculator.calculateBalances(users, expenses);

        SettlementCalculator settlementCalculator = new SettlementCalculator();

        return settlementCalculator.calculateSettlements(users, balances)
                .stream()
                .map(settlement -> new SettlementResponse(
                        settlement.getFromUser().getName(),
                        settlement.getToUser().getName(),
                        settlement.getAmount()
                ))
                .toList();
    }

    private void validateHouseholdExists(int householdId) {
        if (!householdRepository.existsById(householdId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Household not found");
        }
    }

    private List<User> getUsersForHousehold(int householdId) {
        return userRepository.findAll().stream()
                .filter(user -> user.getHousehold() != null && user.getHousehold().getId() == householdId)
                .toList();
    }

    private List<Expense> getExpensesForHousehold(int householdId) {
        List<Expense> expenses = expenseRepository.findByHouseholdId(householdId);

        for (Expense expense : expenses) {
            if (expense.getSplits() == null) {
                expense.setSplits(List.of());
            }
        }

        return expenses;
    }

    @Operation(summary = "Update an expense", description = "Updates an existing expense using the supplied expense details and splits.")
    @ApiResponse(responseCode = "200", description = "Expense updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid expense data")
    @ApiResponse(responseCode = "404", description = "Expense, payer, or household not found")
    @PutMapping("/{expenseId}")
    public Expense updateExpense(
            @Parameter(description = "ID of the expense to update", example = "1") @PathVariable int expenseId,
            @Valid @RequestBody CreateExpenseRequest request
    ) {
        return expenseService.updateExpense(
                expenseId,
                request.description(),
                request.amount(),
                request.paidById(),
                request.householdId(),
                request.splits(),
                request.category(),
                request.date()
        );
    }

    @Operation(summary = "Delete an expense", description = "Deletes the specified expense and returns HTTP 204 when successful.")
    @ApiResponse(responseCode = "204", description = "Expense deleted successfully")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@Parameter(description = "ID of the expense to delete", example = "1") @PathVariable int expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create an expense", description = "Creates an expense with its payer, household, category, date, and split details.")
    @ApiResponse(responseCode = "200", description = "Expense created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid expense data or split amounts")
    @ApiResponse(responseCode = "404", description = "Payer or household not found")
    @PostMapping
    public Expense createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        return expenseService.createExpense(
                request.description(),
                request.amount(),
                request.paidById(),
                request.householdId(),
                request.splits(),
                request.category(),
                request.date()
        );
    }
}