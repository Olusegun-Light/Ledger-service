package com.olusegunlight.ledger.transaction.service;

import com.olusegunlight.ledger.config.LedgerProperties;
import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.EntryFactory;
import com.olusegunlight.ledger.domain.EntryNotFoundException;
import com.olusegunlight.ledger.domain.Ledger;
import com.olusegunlight.ledger.domain.JournalEntry;
import com.olusegunlight.ledger.domain.LedgerCommand.Transfer;
import org.springframework.stereotype.Service;
import com.olusegunlight.ledger.domain.LedgerCommand.Reversal;

@Service
public class TransactionService {

    private final Ledger ledger;
    private final EntryFactory entryFactory;
    private final LedgerProperties properties;

    public TransactionService(Ledger ledger, EntryFactory entryFactory, LedgerProperties properties) {
        this.ledger = ledger;
        this.entryFactory = entryFactory;
        this.properties = properties;
    }

    public JournalEntry transfer(String fromAccountId, String toAccountId, String amount) {
        Money money = Money.fromMajor(amount, properties.currency());

        Money limit = properties.maxTransferAmountValue();
        if(money.isGreaterThan(limit)){
            throw new TransferLimitExceededException(money, limit);
        }
        JournalEntry entry = entryFactory.toEntry(new Transfer(fromAccountId, toAccountId, money, properties.transferFeeAmount()));
        ledger.post(entry);
        return entry;
    }

    public JournalEntry reverse(String entryId) {
        JournalEntry original = ledger.findEntry(entryId).orElseThrow(() -> new EntryNotFoundException(entryId));
        JournalEntry reversal = entryFactory.toEntry(new Reversal(original));
        ledger.post(reversal);
        return reversal;
    }
}
