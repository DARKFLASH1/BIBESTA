package com.example.BIBESTA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BibestaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BibestaApplication.class, args);
	}

}