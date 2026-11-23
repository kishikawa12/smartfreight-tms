package com.smartfreight.tmsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TmsApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TmsApiApplication.class, args);
    }
}
