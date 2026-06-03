package com.avh.practicas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PracticasApplication {

	public static void main(String[] args) {
		SpringApplication.run(PracticasApplication.class, args);
	}

}

