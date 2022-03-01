package com.example.userservice.controller;

import com.example.userservice.user.domain.User;
import com.example.userservice.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/")
public class HelloController {
    private final Environment env;
    private final UserJpaRepository userJpaRepository;

    @GetMapping("/hello")
    public String hello(){
        String applicationName = env.getProperty("spring.application.name");
        log.info("hello application name = {}", applicationName);
        return "hello";
    }

    @GetMapping("/responsecache")
    public String responseCache(){
        log.info("response cache");
        sleep(1000);
        String applicationName = env.getProperty("spring.application.name");
        return applicationName;
    }

    private void sleep(long millis){
        try{
            Thread.sleep(millis);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    //일반 유저 요청
    @GetMapping("/users/{username}")
    public User users(@PathVariable("username") String username){
        log.info("request users");
        User findUser = userJpaRepository.findByUsername(username);
        return findUser;
    }

    //일반 어드민 요청
    @GetMapping("/admins/{username}")
    public User admins(@PathVariable("username") String username){
        log.info("request users");
        User findUser = userJpaRepository.findByUsername(username);
        return findUser;
    }

}
