package com.olusegunlight.ledger.config;

import org.springframework.context.annotation.Profile;
import com.olusegunlight.ledger.account.service.AccountService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DemoDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final AccountService accountService;

    public DemoDataSeeder(AccountService accountService) {
        this.accountService = accountService;
        log.info("1. DemoDataSeeder constructed — dependencies injected");
    }

    @PostConstruct
    void seed() {
        if (accountService.exists("wallet:light")) {
            log.info("2. @PostConstruct — demo wallets already in the database, skipping seed");
            return;
        }
        accountService.openWallet("light", "Light Olusegun");
        accountService.openWallet("ada", "Ada Obi");
        accountService.deposit("wallet:light", "50000.00");
        log.info("2. @PostConstruct — seeded wallet:light (₦50,000.00) and wallet:ada (₦0.00)");
    }

    @PreDestroy
    void shutdown() {
        log.info("3. @PreDestroy — application shutting down, in-memory data will be lost");
    }
}