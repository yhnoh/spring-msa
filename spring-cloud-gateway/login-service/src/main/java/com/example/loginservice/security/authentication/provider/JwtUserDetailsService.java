package com.example.loginservice.security.authentication.provider;

import com.example.loginservice.user.domain.User;
import com.example.loginservice.user.domain.UserToken;
import com.example.loginservice.user.repository.UserJpaRepository;
import com.example.loginservice.user.repository.UserTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userJpaRepository;
    private final UserTokenJpaRepository userTokenJpaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User findUser = userJpaRepository.findByUsername(username);
        if(findUser == null){
            throw new UsernameNotFoundException("user not found");
        }

        UserDetails userDetails = new JwtUserDetails(findUser.getUsername(), findUser.getPassword());

        return userDetails;
    }

    @Transactional
    public UserToken saveUserToken(String username, String token){
        User findUser = userJpaRepository.findByUsername(username);
        LocalDateTime expireTime = JwtUtils.getExpireTimeByToken(token);
        UserToken userToken = new UserToken(findUser, token, expireTime);
        return userTokenJpaRepository.save(userToken);
    }
}
