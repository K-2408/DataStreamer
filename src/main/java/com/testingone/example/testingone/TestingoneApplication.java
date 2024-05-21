package com.testingone.example.testingone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TestingoneApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestingoneApplication.class, args);
	}

}
