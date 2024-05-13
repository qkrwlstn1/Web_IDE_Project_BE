package com.als.webIde;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class WebIdeApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebIdeApplication.class, args);
	}

}
