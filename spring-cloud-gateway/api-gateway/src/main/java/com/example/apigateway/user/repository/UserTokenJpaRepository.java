package com.example.apigateway.user.repository;


import com.example.apigateway.user.domain.UserToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenJpaRepository extends JpaRepository<UserToken, Long> {

    @EntityGraph(attributePaths = {"user"})
    UserToken findByToken(String token);
}
