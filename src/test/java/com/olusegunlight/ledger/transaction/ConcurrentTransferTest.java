package com.olusegunlight.ledger.transaction;

import com.olusegunlight.ledger.account.repository.AccountRepository;
import com.olusegunlight.ledger.account.service.AccountService;
import com.olusegunlight.ledger.common.Currency;
import com.olusegunlight.ledger.common.Money;
import com.olusegunlight.ledger.domain.InsufficientFundsException;
import com.olusegunlight.ledger.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ConcurrentTransferTest {

    @Autowired
    AccountService accountService;

    @Autowired
    TransactionService transactionService;

    @Test
    void concurrentTransfersCannotOverdrawAWallet() throws Exception {
        String run = UUID.randomUUID().toString().substring(0, 8);
        String from = accountService.openWallet("race-from-" + run, "Race From").id();
        String to = accountService.openWallet("race-to-" + run, "Race To").id();
        accountService.deposit(from, "100.00");

        int attempts = 10;
        CountDownLatch startGun = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        try (ExecutorService pool = Executors.newFixedThreadPool(attempts)) {
            for (int i = 0; i < attempts; i++) {
                results.add(pool.submit(() -> {
                    startGun.await();
                    try {
                        transactionService.transfer(from, to, "60.00");
                        return true;
                    } catch (InsufficientFundsException e) {
                        return false;
                    }
                }));
            }
            startGun.countDown();
        }

        int succeeded = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                succeeded++;
            }
        }

        assertThat(succeeded).isEqualTo(1);
        assertThat(accountService.balanceOf(from)).isEqualTo(Money.fromMajor("15.00", Currency.NGN));
    }

    @Test
    void transfersInBothDirectionsAtOnceKeepEveryKoboAccountedFor() throws Exception {
        String run = UUID.randomUUID().toString().substring(0, 8);
        String a = accountService.openWallet("both-a-" + run, "Both A").id();
        String b = accountService.openWallet("both-b-" + run, "Both B").id();
        accountService.deposit(a, "1000.00");
        accountService.deposit(b, "1000.00");

        int transfers = 20;
        CountDownLatch startGun = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();

        try (ExecutorService pool = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < transfers; i++) {
                String from = (i % 2 == 0) ? a : b;
                String to = (i % 2 == 0) ? b : a;
                results.add(pool.submit(() -> {
                    startGun.await();
                    return transactionService.transfer(from, to, "10.00");
                }));
            }
            startGun.countDown();
        }

        for (Future<?> result : results) {
            result.get();
        }

        assertThat(accountService.balanceOf(a)).isEqualTo(Money.fromMajor("750.00", Currency.NGN));
        assertThat(accountService.balanceOf(b)).isEqualTo(Money.fromMajor("750.00", Currency.NGN));
    }
}
