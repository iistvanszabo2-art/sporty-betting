package com.sporty.outcome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventOutcomeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventOutcomeServiceApplication.class, args);
    }
}
