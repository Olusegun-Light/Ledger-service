package com.olusegunlight.ledger.domain;

import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.LedgerCommand.Deposit;
import com.olusegunlight.ledger.domain.LedgerCommand.Transfer;
import com.olusegunlight.ledger.domain.LedgerCommand.Withdrawal;

import java.util.List;
import java.util.UUID;

public class EntryFactory {

    public static final String SETTLEMENT_ACCOUNT = "asset:bank-settlement";
    public static final String FEE_ACCOUNT = "income:fees";

    public JournalEntry toEntry(LedgerCommand command) {
        String id = UUID.randomUUID().toString();

        return switch (command) {
            case Deposit(String walletId, Money amount) -> new JournalEntry(id, "Deposit", List.of(
                    Posting.debit(SETTLEMENT_ACCOUNT, amount),
                    Posting.credit(walletId, amount)));

            case Withdrawal(String walletId, Money amount) -> new JournalEntry(id, "Withdrawal", List.of(
                    Posting.debit(walletId, amount),
                    Posting.credit(SETTLEMENT_ACCOUNT, amount)));

            case Transfer(String from, String to, Money amount, Money fee) when fee.isZero() ->
                    new JournalEntry(id, "Transfer", List.of(
                            Posting.debit(from, amount),
                            Posting.credit(to, amount)));

            case Transfer(String from, String to, Money amount, Money fee) ->
                    new JournalEntry(id, "Transfer with fee", List.of(
                            Posting.debit(from, amount.plus(fee)),
                            Posting.credit(to, amount),
                            Posting.credit(FEE_ACCOUNT, fee)));
        };
    }
}