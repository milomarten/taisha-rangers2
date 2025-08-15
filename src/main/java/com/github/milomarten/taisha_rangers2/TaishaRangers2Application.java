package com.github.milomarten.taisha_rangers2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaishaRangers2Application {

	public static void main(String[] args) {
		SpringApplication.run(TaishaRangers2Application.class, args);
	}

}
