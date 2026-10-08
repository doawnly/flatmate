package com.flatmate.service;

import com.flatmate.model.Settlement;
import com.flatmate.model.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SettlementCalculator {

    public List<Settlement> calculateSettlements(
            List<User> users,
            Map<Integer, BigDecimal> balances) {

        List<Settlement> settlements = new ArrayList<>();

        Map<Integer, BigDecimal> debtors = new HashMap<>();
        Map<Integer, BigDecimal> creditors = new HashMap<>();

        for (Map.Entry<Integer, BigDecimal> entry : balances.entrySet()) {
            BigDecimal balance = entry.getValue();

            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.put(entry.getKey(), balance.abs());
            } else if (balance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.put(entry.getKey(), balance);
            }
        }

        while (!debtors.isEmpty() && !creditors.isEmpty()) {

            Map.Entry<Integer, BigDecimal> debtor = debtors.entrySet().iterator().next();
            Map.Entry<Integer, BigDecimal> creditor = creditors.entrySet().iterator().next();

            User debtorUser = users.stream()
                    .filter(user -> user.getId() == debtor.getKey())
                    .findFirst()
                    .orElseThrow();

            User creditorUser = users.stream()
                    .filter(user -> user.getId() == creditor.getKey())
                    .findFirst()
                    .orElseThrow();

            BigDecimal payment = debtor.getValue().min(creditor.getValue());

            settlements.add(
                    new Settlement(debtorUser, creditorUser, payment)
            );

            BigDecimal debtorRemaining = debtor.getValue().subtract(payment);
            BigDecimal creditorRemaining = creditor.getValue().subtract(payment);

            if (debtorRemaining.compareTo(BigDecimal.ZERO) == 0) {
                debtors.remove(debtor.getKey());
            } else {
                debtors.put(debtor.getKey(), debtorRemaining);
            }

            if (creditorRemaining.compareTo(BigDecimal.ZERO) == 0) {
                creditors.remove(creditor.getKey());
            } else {
                creditors.put(creditor.getKey(), creditorRemaining);
            }
        }

        return settlements;
    }
}