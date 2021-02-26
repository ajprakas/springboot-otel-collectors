package com.ajay.example.userApp;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAutoConfiguration
public class UserApp {
    static int n;
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(UserApp.class, args);
        System.setProperty("otel.resource.attributes", "service.name=UserApp");
        System.setProperty("otlp.resource.attributes", "service.name=UserApp");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper mapper(){
        return new ObjectMapper();
    }

}