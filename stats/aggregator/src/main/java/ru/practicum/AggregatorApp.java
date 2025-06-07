package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.service.AggregationStarter;

@ConfigurationPropertiesScan
@SpringBootApplication
public class AggregatorApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AggregatorApp.class, args);
        AggregationStarter aggregator = context.getBean(AggregationStarter.class);
        aggregator.start();
    }
}