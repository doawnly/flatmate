package com.flatmate.repository;

import com.flatmate.model.Expense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    @Override
    @EntityGraph(attributePaths = "splits")
    List<Expense> findAll();
}