package com.sporty.matcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BetMatcherServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BetMatcherServiceApplication.class, args);
    }
}
