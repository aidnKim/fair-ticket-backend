package com.fairticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // BaseTimeEntity 를 사용하기 위함
@SpringBootApplication
public class FairTicketBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FairTicketBackendApplication.class, args);
	}

}
