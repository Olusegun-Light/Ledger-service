package com.olusegunlight.ledger.persistence;

import com.olusegunlight.ledger.account.entity.AccountEntity;
import com.olusegunlight.ledger.account.repository.AccountRepository;
import com.olusegunlight.ledger.common.CurrencyMismatchException;
import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.*;
import com.olusegunlight.ledger.transaction.entity.JournalEntryEntity;
import com.olusegunlight.ledger.transaction.repository.JournalEntryRepository;
import com.olusegunlight.ledger.transaction.repository.PostingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class PostgresLedger implements Ledger {

    private final AccountRepository accounts;
    private final JournalEntryRepository journalEntries;
    private final PostingRepository postings;

    public PostgresLedger(AccountRepository accounts, JournalEntryRepository journalEntries, PostingRepository postings) {
        this.accounts = accounts;
        this.journalEntries = journalEntries;
        this.postings = postings;
    }

    @Override
    @Transactional
    public void openAccount(LedgerAccount account) {
        if (accounts.existsById(account.id())) {
            throw new AccountAlreadyExistsException(account.id());
        }
        accounts.save(AccountEntity.from(account));
    }

    @Override
    public Optional<LedgerAccount> findAccount(String accountId) {
        return accounts.findById(accountId).map(AccountEntity::toDomain);
    }

    @Override
    @Transactional
    public void post(JournalEntry entry) {
        if (journalEntries.existsById(entry.id())) {
            throw new DuplicateEntryException(entry.id());
        }

        List<LedgerAccount> involved = entry.accountIds().stream().map(this::getAccount).toList();
        for (LedgerAccount account : involved) {
            if (account.currency() != entry.currency()) {
                throw new CurrencyMismatchException(account.currency(), entry.currency());
            }
        }

        List<String> wallets = involved.stream()
                .filter(account -> account.type() == AccountType.LIABILITY)
                .map(LedgerAccount::id)
                .toList();
        accounts.lockAllById(wallets);

        for (LedgerAccount account : involved) {
            if (account.type() != AccountType.LIABILITY) {
                continue;
            }
            if (!inEntry(entry, account, Direction.DEBIT).isZero()) {
                AccountStatus status = accounts.findStatusById(account.id());
                if (status != AccountStatus.ACTIVE) {
                    throw new AccountNotActiveException(account.id(), status);
                }
            }
            if (balanceAfter(account, entry).isNegative()) {
                throw new InsufficientFundsException(account.id());
            }
        }

        journalEntries.save(JournalEntryEntity.from(entry));
    }

    @Override
    public Money balanceOf(String accountId) {
        LedgerAccount account = getAccount(accountId);
        return account.type().balanceFrom(stored(account, Direction.DEBIT), stored(account, Direction.CREDIT));
    }

    @Override
    public Optional<JournalEntry> findEntry(String entryId) {
        return journalEntries.findById(entryId).map(JournalEntryEntity::toDomain);
    }

    @Override
    public List<JournalEntry> entriesFor(String accountId) {
        return journalEntries.findAllTouchingAccount(accountId).stream().map(JournalEntryEntity::toDomain).toList();
    }

    private Money balanceAfter(LedgerAccount account, JournalEntry entry) {
        Money debits = stored(account, Direction.DEBIT).plus(inEntry(entry, account, Direction.DEBIT));
        Money credits = stored(account, Direction.CREDIT).plus(inEntry(entry, account, Direction.CREDIT));
        return account.type().balanceFrom(debits, credits);
    }

    private Money stored(LedgerAccount account, Direction direction) {
        return Money.of(postings.sumAmounts(account.id(), direction), account.currency());
    }

    private static Money inEntry(JournalEntry entry, LedgerAccount account, Direction direction) {
        return entry.postings().stream().filter(posting -> posting.accountId().equals(account.id())).filter(posting -> posting.direction() == direction).map(Posting::amount).reduce(Money.zero(account.currency()), Money::plus);
    }


}
