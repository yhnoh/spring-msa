package com.example.orderservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/order-service")
public class HelloController {
    private final Environment env;

    @GetMapping("/hello")
    public String hello(){
        String applicationName = env.getProperty("spring.application.name");
        log.info("application name = {} ", applicationName);
        return applicationName;
    }

    @GetMapping("/thread/hello")
    public String threadhello(){
        sleep(1000);
        String applicationName = env.getProperty("spring.application.name");
        log.info("application name = {} ", applicationName);
        return applicationName;
    }

    public void sleep(long millis){
        try{
            Thread.sleep(millis);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

}
