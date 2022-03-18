package com.example.configclient;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {
    private final Environment environment;

    public ConfigController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/env")
    public String env(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("spring.profiles.active = " + environment.getProperty("spring.profiles.active"));
        stringBuilder.append("\nenv = " + environment.getProperty("env"));

        return stringBuilder.toString();
    }
}
