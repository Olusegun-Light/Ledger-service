package com.olusegunlight.ledger.transaction.service;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.EntryFactory;
import com.olusegunlight.ledger.domain.InMemoryLedger;
import com.olusegunlight.ledger.domain.JournalEntry;
import com.olusegunlight.ledger.domain.LedgerCommand.Transfer;
import org.springframework.stereotype.Service;
import com.olusegunlight.ledger.domain.LedgerCommand.Reversal;

@Service
public class TransactionService {

    private static final Money TRANSFER_FEE = Money.of(25_00L, Currency.NGN);

    private final InMemoryLedger ledger;
    private final EntryFactory entryFactory;

    public TransactionService(InMemoryLedger ledger, EntryFactory entryFactory) {
        this.ledger = ledger;
        this.entryFactory = entryFactory;
    }

    public JournalEntry transfer(String fromAccountId, String toAccountId, String amount) {
        Money money = Money.fromMajor(amount, Currency.NGN);
        JournalEntry entry = entryFactory.toEntry(new Transfer(fromAccountId, toAccountId, money, TRANSFER_FEE));
        ledger.post(entry);
        return entry;
    }

    public JournalEntry reverse(String entryId) {
        JournalEntry original = ledger.findEntry(entryId).orElseThrow(() -> new IllegalArgumentException(("Journal entry not found " + entryId)));
        JournalEntry reversal = entryFactory.toEntry(new Reversal(original));
        ledger.post(reversal);
        return reversal;
    }
}
