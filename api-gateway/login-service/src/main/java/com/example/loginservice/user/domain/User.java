package com.example.loginservice.user.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserAuthorize authorize;

    public User(Long id, String username, String password, UserAuthorize authorize) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorize = authorize;
    }
}
