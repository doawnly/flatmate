package com.flatmate.service;

import com.flatmate.controller.ResourceNotFoundException;
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
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Expense description must not be blank");
        }

        if (splits == null || splits.isEmpty()) {
            throw new IllegalArgumentException("At least one expense split is required");
        }

        if (splits.stream().anyMatch(split ->
                split.amountOwed() == null ||
                        split.amountOwed().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Split amounts must be greater than zero");
        }

        User paidBy = userRepository.findById(paidById)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Payer not found: " + paidById
        ));

        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Household not found: " + householdId
        ));

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
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Split user not found: " + splitRequest.userId()
                    ));

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