package com.nyam.everyday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Clock;
import java.time.ZoneId;

@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
//@EnableAsync// 비동기처리
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}


	@Bean
	public Clock systemClock() {
		return Clock.system(ZoneId.of("Asia/Seoul"));
	}
}
