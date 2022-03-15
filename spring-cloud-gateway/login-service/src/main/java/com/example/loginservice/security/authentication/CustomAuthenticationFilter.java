package com.example.loginservice.security.authentication;

import com.example.loginservice.security.authentication.provider.JwtUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }


    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!isAjax(request) && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("attemptAuthentication need XMLHttpRequest and Post: " + request.getMethod());
        }

        JwtUserDetails jwtUser = getJwtUser(request);
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(jwtUser.getUsername(), jwtUser.getPassword());
        this.setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);

    }

    private JwtUserDetails getJwtUser(HttpServletRequest request) throws AuthenticationException {
        ObjectMapper mapper = new ObjectMapper();
        JwtUserDetails jwtUser = null;
        try{
            jwtUser = mapper.readValue(request.getInputStream(), JwtUserDetails.class);
        }catch (IOException e){
            throw new UsernameNotFoundException("user parsing error");
        }
        return jwtUser;

    }


    private boolean isAjax(HttpServletRequest request){
        return request.getHeader("x-requested-with").equals("XMLHttpRequest");
    }


}
