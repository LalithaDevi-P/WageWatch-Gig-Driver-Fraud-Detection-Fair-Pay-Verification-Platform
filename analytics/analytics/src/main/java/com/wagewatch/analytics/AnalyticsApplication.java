package com.wagewatch.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnalyticsApplication {

	public static void main(String[] args) {
		String apiKey = System.getenv("GEMINI_API_KEY");
		System.out.println("DEBUG: API Key detected: " + (apiKey != null && !apiKey.isEmpty()));
		SpringApplication.run(AnalyticsApplication.class, args);
	}

}
