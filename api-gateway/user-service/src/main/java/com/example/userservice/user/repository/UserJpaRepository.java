package com.example.userservice.user.repository;


import com.example.userservice.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
}
