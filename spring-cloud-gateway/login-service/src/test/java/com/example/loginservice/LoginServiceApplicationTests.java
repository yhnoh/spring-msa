package com.example.loginservice;

import com.example.loginservice.security.authentication.provider.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

//@SpringBootTest
class LoginServiceApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(UUID.randomUUID().toString());
    }

    @Test
    public void createJwt(){
        String username = "username";
        System.out.println(JwtUtils.createJwtToken(username));
    }
}
