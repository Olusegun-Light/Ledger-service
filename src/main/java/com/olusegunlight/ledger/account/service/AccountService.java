package com.olusegunlight.ledger.account.service;

import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.config.LedgerProperties;
import com.olusegunlight.ledger.domain.AccountType;
import com.olusegunlight.ledger.domain.EntryFactory;
import com.olusegunlight.ledger.domain.JournalEntry;
import com.olusegunlight.ledger.domain.Ledger;
import com.olusegunlight.ledger.domain.LedgerAccount;
import com.olusegunlight.ledger.domain.LedgerCommand.Deposit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final Ledger ledger;
    private final EntryFactory entryFactory;
    private final LedgerProperties properties;

    public AccountService(Ledger ledger, EntryFactory entryFactory, LedgerProperties properties) {
        this.ledger = ledger;
        this.entryFactory = entryFactory;
        this.properties = properties;
    }

    public LedgerAccount openWallet(String handle, String name) {
        LedgerAccount wallet = new LedgerAccount("wallet:" + handle, name, AccountType.LIABILITY, properties.currency());
        ledger.openAccount(wallet);
        return wallet;
    }

    public boolean exists(String accountId) {
        return ledger.findAccount(accountId).isPresent();
    }

    public LedgerAccount getAccount(String accountId){
        return ledger.getAccount(accountId);
    }

    public Money balanceOf(String accountId) {
        return ledger.balanceOf(accountId);
    }

    public Money deposit(String walletId, String amount) {
        Money money = Money.fromMajor(amount, properties.currency());
        ledger.post(entryFactory.toEntry(new Deposit(walletId, money)));
        return ledger.balanceOf(walletId);
    }

    public List<JournalEntry> statement(String accountId) {
        ledger.getAccount(accountId);
        return ledger.entriesFor(accountId);
    }
}