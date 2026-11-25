package com.olusegunlight.ledger.persistence;

import com.olusegunlight.ledger.account.entity.AccountEntity;
import com.olusegunlight.ledger.account.repository.AccountRepository;
import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.AccountStatus;
import com.olusegunlight.ledger.domain.AccountType;
import com.olusegunlight.ledger.domain.Direction;
import com.olusegunlight.ledger.domain.JournalEntry;
import com.olusegunlight.ledger.transaction.entity.JournalEntryEntity;
import com.olusegunlight.ledger.transaction.repository.JournalEntryRepository;
import com.olusegunlight.ledger.transaction.repository.PostingRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LedgerRepositoriesTest {

    @Autowired
    AccountRepository accounts;

    @Autowired
    JournalEntryRepository journalEntries;

    @Autowired
    PostingRepository postings;

    @Autowired
    EntityManager entityManager;

    @Test
    void houseAccountsComeFromTheFlywaySeedMigration() {
        assertThat(accounts.findById("income:fees"))
                .get()
                .extracting(AccountEntity::getType, AccountEntity::getStatus)
                .containsExactly(AccountType.INCOME, AccountStatus.ACTIVE);
    }

    @Test
    void savesAndReloadsAnAccount() {
        accounts.save(new AccountEntity("wallet:test", "Test User", AccountType.LIABILITY, Currency.NGN));
        entityManager.flush();
        entityManager.clear();

        AccountEntity reloaded = accounts.findById("wallet:test").orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Test User");
        assertThat(reloaded.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void savesAJournalEntryWithItsPostingsAndMapsItBack() {
        accounts.save(new AccountEntity("wallet:test", "Test User", AccountType.LIABILITY, Currency.NGN));
        JournalEntry deposit = JournalEntry.builder("entry-1", "Deposit")
                .debit("asset:bank-settlement", Money.of(5_000_00, Currency.NGN))
                .credit("wallet:test", Money.of(5_000_00, Currency.NGN))
                .build();

        journalEntries.save(JournalEntryEntity.from(deposit));
        entityManager.flush();
        entityManager.clear();

        JournalEntry reloaded = journalEntries.findById("entry-1").orElseThrow().toDomain();
        assertThat(reloaded).isEqualTo(deposit);
    }

    @Test
    void sumsPostingsPerAccountAndDirection() {
        accounts.save(new AccountEntity("wallet:test", "Test User", AccountType.LIABILITY, Currency.NGN));
        journalEntries.save(JournalEntryEntity.from(JournalEntry.builder("entry-1", "Deposit")
                .debit("asset:bank-settlement", Money.of(5_000_00, Currency.NGN))
                .credit("wallet:test", Money.of(5_000_00, Currency.NGN))
                .build()));
        journalEntries.save(JournalEntryEntity.from(JournalEntry.builder("entry-2", "Deposit")
                .debit("asset:bank-settlement", Money.of(2_500_00, Currency.NGN))
                .credit("wallet:test", Money.of(2_500_00, Currency.NGN))
                .build()));
        entityManager.flush();

        assertThat(postings.sumAmounts("wallet:test", Direction.CREDIT)).isEqualTo(7_500_00);
        assertThat(postings.sumAmounts("wallet:test", Direction.DEBIT)).isZero();
        assertThat(journalEntries.findAllTouchingAccount("wallet:test"))
                .extracting(JournalEntryEntity::getId)
                .containsExactly("entry-1", "entry-2");
    }

    @Test
    void freezingAnAccountIsSavedWithoutCallingSave() {
        accounts.save(new AccountEntity("wallet:test", "Test User", AccountType.LIABILITY, Currency.NGN));
        entityManager.flush();
        entityManager.clear();

        AccountEntity account = accounts.findById("wallet:test").orElseThrow();
        account.freeze();
        entityManager.flush();
        entityManager.clear();

        assertThat(accounts.findByStatus(AccountStatus.FROZEN))
                .extracting(AccountEntity::getId)
                .containsExactly("wallet:test");
    }

    @Test
    void freezingTwiceKeepsTheAccountFrozen() {
        AccountEntity account = new AccountEntity("wallet:test", "Test User", AccountType.LIABILITY, Currency.NGN);
        account.freeze();
        account.freeze();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.FROZEN);
    }
}
