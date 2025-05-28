package com.example.eureka_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ClientService1Application {
    public static void main(String[] args) {
        SpringApplication.run(ClientService1Application.class, args);
    }
}

