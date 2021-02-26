package com.ajay.example.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class SpringbootMongoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootMongoApplication.class, args);
	}

}
