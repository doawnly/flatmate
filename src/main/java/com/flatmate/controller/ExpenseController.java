package com.flatmate.controller;

import com.flatmate.model.Expense;
import com.flatmate.repository.ExpenseRepository;
import com.flatmate.service.ExpenseService;
import com.flatmate.model.User;
import com.flatmate.repository.UserRepository;
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

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    public ExpenseController(
            ExpenseRepository expenseRepository,
            ExpenseService expenseService,
            UserRepository userRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    @GetMapping("/balances")
    public Map<String, BigDecimal> getBalances() {
        return getBalancesForHousehold(1);
    }

    @GetMapping("/settlements")
    public List<SettlementResponse> getSettlements() {
        return getSettlementsForHousehold(1);
    }

    @GetMapping("/households/{householdId}/balances")
    public Map<String, BigDecimal> getBalancesByHousehold(@PathVariable int householdId) {
        return getBalancesForHousehold(householdId);
    }

    @GetMapping("/households/{householdId}/settlements")
    public List<SettlementResponse> getSettlementsByHousehold(@PathVariable int householdId) {
        return getSettlementsForHousehold(householdId);
    }

    private Map<String, BigDecimal> getBalancesForHousehold(int householdId) {
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

    @PostMapping
    public Expense createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        return expenseService.createExpense(
                request.description(),
                request.amount(),
                request.paidById(),
                request.householdId(),
                request.splits()
        );
    }
}