package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "ru/practicum/client")
@SpringBootApplication
public class RequestServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApp.class, args);
    }

}
