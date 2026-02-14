package com.burnafter.burnafter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BurnAfterApplication {

	public static void main(String[] args) {
		SpringApplication.run(BurnAfterApplication.class, args);
	}

}
