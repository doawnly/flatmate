package com.flatmate.service;

import com.flatmate.model.Expense;
import com.flatmate.model.ExpenseSplit;
import com.flatmate.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BalanceCalculator {
    public Map<Integer, BigDecimal> calculateBalances(List<User> users, List<Expense> expenses){
        Map<Integer, BigDecimal> balances = new HashMap<>();

        for (User user : users){
            balances.put(user.getId(), BigDecimal.ZERO);
        }

        for (Expense expense: expenses){
            User payer = expense.getPaidBy();
            BigDecimal amountPaid = expense.getAmount();

            balances.put(
                payer.getId(),
                balances.get(payer.getId()).add(amountPaid)
            );

            for (ExpenseSplit split : expense.getSplits()){
                User user = split.getUser();
                BigDecimal amountOwed = split.getAmountOwed();

                balances.put(
                    user.getId(),
                    balances.get(user.getId()).subtract(amountOwed)
                );
            }
        }
        return balances;
    }
}
