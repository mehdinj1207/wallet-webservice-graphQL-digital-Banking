package com.bank.demo;

import com.bank.demo.service.WalletService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(EBankApplication.class, args);
	}

	@Bean
	CommandLineRunner start (WalletService walletService ) {
		return args -> {
			walletService.loadData();
		};
	}

}
