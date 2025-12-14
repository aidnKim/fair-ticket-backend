package com.fairticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing // BaseTimeEntity 를 사용하기 위함
@EnableScheduling // 스케줄러 사용하기 위함
@SpringBootApplication
public class FairTicketBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FairTicketBackendApplication.class, args);
	}

}
