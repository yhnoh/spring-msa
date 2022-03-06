package com.example.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        //로깅
        System.setProperty("reactor.netty.http.server.accessLogEnabled", "true");
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
