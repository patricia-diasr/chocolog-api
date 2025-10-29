package com.chocolog.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ChocologApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChocologApiApplication.class, args);
	}

}
