package com.example.loginservice.security;

import com.example.loginservice.security.authentication.CustomAuthenticationFilter;
import com.example.loginservice.security.authentication.NoPasswordEncoder;
import com.example.loginservice.security.authentication.provider.JwtAuthenticationProvider;
import com.example.loginservice.security.authentication.provider.JwtUserDetails;
import com.example.loginservice.security.authentication.provider.JwtUserDetailsService;
import com.example.loginservice.user.domain.User;
import com.example.loginservice.user.repository.UserJpaRepository;
import com.example.loginservice.user.repository.UserTokenJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtUserDetailsService jwtUserDetailsService;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable();
        http.csrf().disable();
        http.authorizeRequests().anyRequest().authenticated();
        http.formLogin().disable();
        //세션 사용을 하지 않는다.
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/h2-console/**", "/resources/**");

    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(){
        return new JwtAuthenticationProvider(jwtUserDetailsService, passwordEncoder());
    }
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(jwtAuthenticationProvider());
    }

    @Bean
    public UsernamePasswordAuthenticationFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(authenticationManager());
        filter.setAuthenticationSuccessHandler(new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

                JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();

                jwtUserDetailsService.saveUserToken(userDetails.getUsername(), userDetails.getToken());

                response.setStatus(HttpStatus.OK.value());

                Map<String, Object> body = new HashMap<>();
                body.put("token", userDetails.getToken());

                ObjectMapper mapper = new ObjectMapper();
                String strBody = mapper.writeValueAsString(body);

                PrintWriter writer = response.getWriter();
                writer.write(strBody);

            }
        });

        filter.setAuthenticationFailureHandler(new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());


                Map<String, Object> body = new HashMap<>();
                body.put("message", exception.getMessage());

                ObjectMapper mapper = new ObjectMapper();
                String strBody = mapper.writeValueAsString(body);

                PrintWriter writer = response.getWriter();
                writer.write(strBody);

            }
        });

        filter.setFilterProcessesUrl("/login");
        filter.afterPropertiesSet();
        return filter;
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new NoPasswordEncoder();
    }

}
