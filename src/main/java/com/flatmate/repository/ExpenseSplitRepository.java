package com.flatmate.repository;

import com.flatmate.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Integer> {

    List<ExpenseSplit> findByExpenseId(int expenseId);
}