package com.flatmate.repository;

import com.flatmate.model.Expense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.flatmate.model.ExpenseCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    @Override
    @EntityGraph(attributePaths = "splits")
    List<Expense> findAll();

    @EntityGraph(attributePaths = {"splits", "paidBy", "household"})
    List<Expense> findByHouseholdId(int householdId);

    @EntityGraph(attributePaths = {"splits", "paidBy", "household"})
    @Query("SELECT e FROM Expense e " +
            "WHERE e.household.id = :householdId " +
            "AND (:#{#category == null} = true OR e.category = :category) " +
            "AND (:#{#startDate == null} = true OR e.date >= :startDate) " +
            "AND (:#{#endDate == null} = true OR e.date <= :endDate)")
    List<Expense> findByFilters(
            @Param("householdId") int householdId,
            @Param("category") ExpenseCategory category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Sort sort
    );
}