package com.flatmate.service;

import com.flatmate.model.Expense;
import com.flatmate.controller.ExpenseSplitRequest;
import com.flatmate.model.ExpenseSplit;
import com.flatmate.model.Household;
import com.flatmate.model.User;
import com.flatmate.repository.ExpenseRepository;
import com.flatmate.repository.ExpenseSplitRepository;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final ExpenseSplitRepository expenseSplitRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            HouseholdRepository householdRepository,
            ExpenseSplitRepository expenseSplitRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.expenseSplitRepository = expenseSplitRepository;
    }

    public Expense createExpense(
            String description,
            BigDecimal amount,
            int paidById,
            int householdId,
            List<ExpenseSplitRequest> splits
    ) {
        User paidBy = userRepository.findById(paidById)
                .orElseThrow();

        Household household = householdRepository.findById(householdId)
                .orElseThrow();

        BigDecimal splitTotal = splits.stream()
                .map(ExpenseSplitRequest::amountOwed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (splitTotal.compareTo(amount) != 0) {
            throw new IllegalArgumentException("Split amounts must add up to the expense amount");
        }

        Expense expense = new Expense(
                description,
                amount,
                paidBy,
                household
        );

        Expense savedExpense = expenseRepository.save(expense);

        for (ExpenseSplitRequest splitRequest : splits) {
            User user = userRepository.findById(splitRequest.userId())
                    .orElseThrow();

            ExpenseSplit split = new ExpenseSplit(
                    savedExpense,
                    user,
                    splitRequest.amountOwed()
            );

            expenseSplitRepository.save(split);
        }

        return savedExpense;
    }
}