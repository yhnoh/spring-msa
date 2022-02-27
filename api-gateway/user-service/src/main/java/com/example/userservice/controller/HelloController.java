package com.example.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user-service")
public class HelloController {
    private final Environment env;

    @GetMapping("/hello")
    public String hello(){
        String applicationName = env.getProperty("spring.application.name");
        log.info("application name = {} ", applicationName);
        return applicationName;
    }

    //동일한 요청에 대한 불필요한 반복 작업을 줄일 수 있는 캐싱
    @GetMapping("/thread/hello")
    public String threadhello(){
        sleep(1000);
        String applicationName = env.getProperty("spring.application.name");
        log.info("application name = {} ", applicationName);
        return applicationName;
    }

    @GetMapping("/thread/hello2")
    public String threadhello2(){
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
