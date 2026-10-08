package com.flatmate.controller;

import com.flatmate.model.Expense;
import com.flatmate.repository.ExpenseRepository;
import com.flatmate.service.ExpenseService;
import com.flatmate.model.User;
import com.flatmate.repository.UserRepository;
import com.flatmate.service.BalanceCalculator;
import com.flatmate.model.Settlement;
import com.flatmate.service.SettlementCalculator;
import com.flatmate.model.SettlementResponse;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

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

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException exception) {
        return exception.getMessage();
    }

    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    @GetMapping("/balances")
    public Map<String, BigDecimal> getBalances() {
        List<User> users = userRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();

        for (Expense expense : expenses) {
            if (expense.getSplits() == null) {
                expense.setSplits(List.of());
            }
        }

        BalanceCalculator calculator = new BalanceCalculator();
        Map<Integer, BigDecimal> balances = calculator.calculateBalances(users, expenses);

        Map<String, BigDecimal> namedBalances = new java.util.HashMap<>();
        for (User user : users) {
            namedBalances.put(user.getName(), balances.get(user.getId()));
        }

        return namedBalances;
    }

    @GetMapping("/settlements")
    public List<SettlementResponse> getSettlements() {
        List<User> users = userRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();

        for (Expense expense : expenses) {
            if (expense.getSplits() == null) {
                expense.setSplits(List.of());
            }
        }

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

    @PostMapping
    public Expense createExpense(@RequestBody CreateExpenseRequest request) {
        return expenseService.createExpense(
                request.description(),
                request.amount(),
                request.paidById(),
                request.householdId(),
                request.splits()
        );
    }
}