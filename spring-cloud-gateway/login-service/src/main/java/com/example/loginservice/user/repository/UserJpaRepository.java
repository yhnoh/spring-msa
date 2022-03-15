package com.example.loginservice.user.repository;

import com.example.loginservice.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
}
