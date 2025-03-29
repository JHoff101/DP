package com.bank.application.uzivatel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.bank.application.uzivatel.Entity")
public class UzivatelApp {

	public static void main(String[] args) {
		SpringApplication.run(UzivatelApp.class, args);
	}

}
