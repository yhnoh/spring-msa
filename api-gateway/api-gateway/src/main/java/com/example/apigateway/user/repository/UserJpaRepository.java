package com.example.apigateway.user.repository;

import com.example.apigateway.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
}
