package com.olusegunlight.ledger.config;

import com.olusegunlight.ledger.domain.EntryFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LedgerProperties.class)
public class LedgerConfig {

    @Bean
    public EntryFactory entryFactory() {
        return new EntryFactory();
    }

}
