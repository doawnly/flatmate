package com.flatmate.service;

import com.flatmate.model.Expense;
import com.flatmate.model.ExpenseSplit;
import com.flatmate.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BalanceCalculator {
    public Map<User, BigDecimal> calculateBalances(List<User> users, List<Expense> expenses){
        Map<User, BigDecimal> balances = new HashMap<>();

        for (User user : users){
            balances.put(user, BigDecimal.ZERO);
        }

        for (Expense expense: expenses){
            User payer = expense.getPaidBy();
            BigDecimal amountPaid = expense.getAmount();

            balances.put(payer,balances.get(payer).add(amountPaid));

            for (ExpenseSplit split : expense.getSplits()){
                User user = split.getUser();
                BigDecimal amountOwed = split.getAmountOwed();

                balances.put(user,balances.get(user).subtract(amountOwed));
            }
        }
        return balances;
    }
}
