package com.example.cloud.care;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class CloudCareApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudCareApplication.class, args);
	}

	// Using the application's AsyncConfig for task executor. No local bean to avoid duplicate bean definition.
}
