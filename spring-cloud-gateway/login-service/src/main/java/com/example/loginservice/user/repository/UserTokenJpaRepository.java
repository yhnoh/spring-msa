package com.example.loginservice.user.repository;

import com.example.loginservice.user.domain.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenJpaRepository extends JpaRepository<UserToken, Long> {
}
