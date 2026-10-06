package com.flatmate.service;

import com.flatmate.model.Expense;
import com.flatmate.model.Household;
import com.flatmate.model.User;
import com.flatmate.repository.ExpenseRepository;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            HouseholdRepository householdRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
    }

    public Expense createExpense(
            String description,
            BigDecimal amount,
            int paidById,
            int householdId
    ) {
        User paidBy = userRepository.findById(paidById)
                .orElseThrow();

        Household household = householdRepository.findById(householdId)
                .orElseThrow();

        Expense expense = new Expense(
                description,
                amount,
                paidBy,
                household
        );

        return expenseRepository.save(expense);
    }
}