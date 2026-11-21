package com.olusegunlight.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.olusegunlight.ledger.common.Naira;

@SpringBootApplication
public class LedgerServiceApplication {

	public static void main(String[] args) {
		System.out.println(Naira.format(1_234_567L));
		SpringApplication.run(LedgerServiceApplication.class, args);
	}

}