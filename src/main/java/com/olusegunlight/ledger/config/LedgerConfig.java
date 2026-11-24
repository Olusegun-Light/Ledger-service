package com.olusegunlight.ledger.config;

import com.olusegunlight.ledger.domain.AccountType;
import com.olusegunlight.ledger.domain.ChartOfAccounts;
import com.olusegunlight.ledger.domain.EntryFactory;
import com.olusegunlight.ledger.domain.InMemoryLedger;
import com.olusegunlight.ledger.domain.LedgerAccount;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LedgerProperties.class)
public class LedgerConfig {

    @Bean
    public ChartOfAccounts chartOfAccounts(LedgerProperties properties) {
        ChartOfAccounts chart = new ChartOfAccounts();

        chart.add(new LedgerAccount(EntryFactory.SETTLEMENT_ACCOUNT, "Bank settlement", AccountType.ASSET, properties.currency()));

        chart.add(new LedgerAccount(EntryFactory.FEE_ACCOUNT, "Fee income", AccountType.INCOME, properties.currency()));
        return chart;
    }

    @Bean
    public InMemoryLedger inMemoryLedger(ChartOfAccounts chartOfAccounts) {
        return new InMemoryLedger(chartOfAccounts);
    }

    @Bean
    public EntryFactory entryFactory() {
        return new EntryFactory();
    }
}
