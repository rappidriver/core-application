package com.rappidrive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RappiDriveApplication {

    public static void main(String[] args) {
        SpringApplication.run(RappiDriveApplication.class, args);
    }

}
