package com.example.loginservice.security.authentication.provider;

import io.jsonwebtoken.Claims;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;

@NoArgsConstructor
public class JwtUserDetails implements UserDetails {

    private String username;
    private String password;
    private String token;
    private LocalDateTime expireTime;

    public JwtUserDetails(String username, String password) {
        this.username = username;
        this.password = password;

        token = JwtUtils.createJwtToken(username);
        expireTime = JwtUtils.getExpireTimeByToken(token);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
