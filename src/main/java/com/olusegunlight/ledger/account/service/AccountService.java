package com.olusegunlight.ledger.account.service;

import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.AccountType;
import com.olusegunlight.ledger.domain.ChartOfAccounts;
import com.olusegunlight.ledger.domain.EntryFactory;
import com.olusegunlight.ledger.domain.InMemoryLedger;
import com.olusegunlight.ledger.domain.LedgerAccount;
import com.olusegunlight.ledger.domain.LedgerCommand.Deposit;
import org.springframework.stereotype.Service;
import com.olusegunlight.ledger.domain.JournalEntry;

import java.util.List;

@Service
public class AccountService {

    private final ChartOfAccounts chart;
    private final InMemoryLedger ledger;
    private final EntryFactory entryFactory;

    public AccountService(ChartOfAccounts chart, InMemoryLedger ledger, EntryFactory entryFactory) {
        this.chart = chart;
        this.ledger = ledger;
        this.entryFactory = entryFactory;
    }

    public LedgerAccount openWallet(String handle, String name) {
        LedgerAccount wallet = new LedgerAccount("wallet:" + handle, name, AccountType.LIABILITY, Currency.NGN);
        chart.add(wallet);
        return wallet;
    }

    public LedgerAccount getAccount(String accountId){
        return chart.get(accountId);
    }

    public Money balanceOf(String accountId) {
        return ledger.balanceOf(accountId);
    }

    public Money deposit(String walletId, String amount) {
        Money money = Money.fromMajor(amount, Currency.NGN);
        ledger.post(entryFactory.toEntry(new Deposit(walletId, money)));
        return ledger.balanceOf(walletId);
    }

    public List<JournalEntry> statement(String accountId) {
        chart.get(accountId);
        return ledger.entriesFor(accountId);
    }
}