package com.flatmate.service;

import com.flatmate.controller.ResourceNotFoundException;
import com.flatmate.model.Expense;
import com.flatmate.model.ExpenseCategory;
import com.flatmate.controller.ExpenseSplitRequest;
import com.flatmate.model.ExpenseSplit;
import com.flatmate.model.Household;
import com.flatmate.model.User;
import com.flatmate.repository.ExpenseRepository;
import com.flatmate.repository.ExpenseSplitRepository;
import com.flatmate.repository.HouseholdRepository;
import com.flatmate.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.data.domain.Sort;

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

    @Transactional(readOnly = true)
    public List<Expense> getExpensesByHouseholdId(int householdId) {
        if (!householdRepository.existsById(householdId)) {
            throw new ResourceNotFoundException("Household not found: " + householdId);
        }

        return expenseRepository.findByHouseholdId(householdId);
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesByHouseholdId(
            int householdId,
            ExpenseCategory category,
            LocalDate startDate,
            LocalDate endDate,
            Sort sort
    ) {
        if (!householdRepository.existsById(householdId)) {
            throw new ResourceNotFoundException("Household not found: " + householdId);
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be on or before endDate");
        }

        return expenseRepository.findByFilters(householdId, category, startDate, endDate, sort);
    }

    @Transactional
    public Expense createExpense(
            String description,
            BigDecimal amount,
            int paidById,
            int householdId,
            List<ExpenseSplitRequest> splits,
            ExpenseCategory category,
            LocalDate date
    ) {
        Expense expense = createExpense(description, amount, paidById, householdId, splits);
        expense.setCategory(category == null ? ExpenseCategory.OTHER : category);
        expense.setDate(date == null ? LocalDate.now() : date);
        return expenseRepository.save(expense);
    }

    @Transactional
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

        long uniqueUserCount = splits.stream()
                .map(ExpenseSplitRequest::userId)
                .distinct()
                .count();

        if (uniqueUserCount != splits.size()) {
            throw new IllegalArgumentException(
                    "A user can only appear once in an expense split"
            );
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

        if (paidBy.getHousehold() == null ||
                paidBy.getHousehold().getId() != household.getId()) {
            throw new IllegalArgumentException(
                    "Payer must belong to the specified household"
            );
        }

        BigDecimal splitTotal = splits.stream()
                .map(ExpenseSplitRequest::amountOwed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (splitTotal.compareTo(amount) != 0) {
            throw new IllegalArgumentException("Split amounts must add up to the expense amount");
        }

        List<User> splitUsers = new ArrayList<>();

        for (ExpenseSplitRequest splitRequest : splits) {
            User user = userRepository.findById(splitRequest.userId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Split user not found: " + splitRequest.userId()
                    ));

            if (user.getHousehold() == null ||
                    user.getHousehold().getId() != household.getId()) {
                throw new IllegalArgumentException(
                        "Split user must belong to the specified household: " + user.getId()
                );
            }

            splitUsers.add(user);
        }

        Expense expense = new Expense(
                description,
                amount,
                paidBy,
                household
        );

        Expense savedExpense = expenseRepository.save(expense);

        for (int i = 0; i < splits.size(); i++) {
            ExpenseSplitRequest splitRequest = splits.get(i);
            User user = splitUsers.get(i);

            ExpenseSplit split = new ExpenseSplit(
                    savedExpense,
                    user,
                    splitRequest.amountOwed()
            );

            expenseSplitRepository.save(split);
        }

        return savedExpense;
    }

    @Transactional
    public Expense updateExpense(
            int expenseId,
            String description,
            BigDecimal amount,
            int paidById,
            int householdId,
            List<ExpenseSplitRequest> splits,
            ExpenseCategory category,
            LocalDate date
    ) {
        Expense expense = updateExpense(expenseId, description, amount, paidById, householdId, splits);
        expense.setCategory(category == null ? ExpenseCategory.OTHER : category);
        expense.setDate(date == null ? LocalDate.now() : date);
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense updateExpense(
            int expenseId,
            String description,
            BigDecimal amount,
            int paidById,
            int householdId,
            List<ExpenseSplitRequest> splits
    ) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense not found: " + expenseId
                ));

        int existingHouseholdId = expense.getHousehold().getId();
        if (householdId != existingHouseholdId) {
            throw new IllegalArgumentException(
                    "An expense cannot be moved to another household"
            );
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Expense description must not be blank");
        }

        if (splits == null || splits.isEmpty()) {
            throw new IllegalArgumentException("At least one expense split is required");
        }

        long uniqueUserCount = splits.stream()
                .map(ExpenseSplitRequest::userId)
                .distinct()
                .count();

        if (uniqueUserCount != splits.size()) {
            throw new IllegalArgumentException(
                    "A user can only appear once in an expense split"
            );
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

        if (paidBy.getHousehold() == null ||
                paidBy.getHousehold().getId() != existingHouseholdId) {
            throw new IllegalArgumentException(
                    "Payer must belong to the specified household"
            );
        }

        BigDecimal splitTotal = splits.stream()
                .map(ExpenseSplitRequest::amountOwed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (splitTotal.compareTo(amount) != 0) {
            throw new IllegalArgumentException("Split amounts must add up to the expense amount");
        }

        List<User> splitUsers = new ArrayList<>();
        for (ExpenseSplitRequest splitRequest : splits) {
            User user = userRepository.findById(splitRequest.userId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Split user not found: " + splitRequest.userId()
                    ));

            if (user.getHousehold() == null ||
                    user.getHousehold().getId() != existingHouseholdId) {
                throw new IllegalArgumentException(
                        "Split user must belong to the specified household: " + user.getId()
                );
            }

            splitUsers.add(user);
        }

        expenseSplitRepository.deleteAll(expenseSplitRepository.findByExpenseId(expenseId));

        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setPaidBy(paidBy);

        Expense savedExpense = expenseRepository.save(expense);

        for (int i = 0; i < splits.size(); i++) {
            ExpenseSplitRequest splitRequest = splits.get(i);
            ExpenseSplit split = new ExpenseSplit(
                    savedExpense,
                    splitUsers.get(i),
                    splitRequest.amountOwed()
            );
            expenseSplitRepository.save(split);
        }

        return savedExpense;
    }

    @Transactional
    public void deleteExpense(int expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense not found: " + expenseId
                ));

        expenseSplitRepository.deleteAll(expenseSplitRepository.findByExpenseId(expenseId));
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSpendingAnalytics(
            int householdId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Expense> expenses = getExpensesByHouseholdId(
                householdId,
                null,
                startDate,
                endDate,
                Sort.by(Sort.Direction.ASC, "date")
        );

        BigDecimal totalSpending = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryTotals = new TreeMap<>();
        Map<String, BigDecimal> monthlyTotals = new TreeMap<>();

        for (Expense expense : expenses) {
            BigDecimal amount = expense.getAmount();
            totalSpending = totalSpending.add(amount);

            String category = expense.getCategory() == null
                    ? ExpenseCategory.OTHER.name()
                    : expense.getCategory().name();
            categoryTotals.merge(category, amount, BigDecimal::add);

            if (expense.getDate() != null) {
                String month = YearMonth.from(expense.getDate()).toString();
                monthlyTotals.merge(month, amount, BigDecimal::add);
            }
        }

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("householdId", householdId);
        analytics.put("startDate", startDate);
        analytics.put("endDate", endDate);
        analytics.put("totalSpending", totalSpending);
        analytics.put("expenseCount", expenses.size());
        analytics.put("categoryTotals", categoryTotals);
        analytics.put("monthlyTotals", monthlyTotals);
        return analytics;
    }
}