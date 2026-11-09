package com.alertaenchentes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlertaEnchentesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertaEnchentesApplication.class, args);
    }

}