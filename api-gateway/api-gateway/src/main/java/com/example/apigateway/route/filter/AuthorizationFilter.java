package com.example.apigateway.route.filter;

import com.example.apigateway.filter.GlobalFilter;
import com.example.apigateway.user.domain.UserAuthorize;
import com.example.apigateway.user.domain.UserToken;
import com.example.apigateway.user.repository.UserJpaRepository;
import com.example.apigateway.user.repository.UserTokenJpaRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static com.example.apigateway.route.filter.AuthorizationFilter.*;

@Slf4j
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<Config> {
    private final UserTokenJpaRepository userTokenJpaRepository;
    AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Transactional(readOnly = true)
    UserToken findLoginUser(String token){
        UserToken userToken = userTokenJpaRepository.findByToken(token);
        return userToken;
    }

    public boolean isUserPath(String path){
        return antPathMatcher.match("/user-service/users/**", path);
    }

    public boolean isAdminPath(String path){
        return antPathMatcher.match("/user-service/admins/**", path);
    }

    public boolean isAuthorizationPath(String path){
        return isUserPath(path) || isAdminPath(path);
    }

    public AuthorizationFilter(UserTokenJpaRepository userTokenJpaRepository) {
        super(Config.class);
        this.userTokenJpaRepository = userTokenJpaRepository;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            String path = request.getPath().toString();

            if(!isAuthorizationPath(path)){
                return chain.filter(exchange).then(Mono.fromRunnable((Runnable) () -> {

                }));
            }
            //인가 키가 없을 경우
            if(!exchange.getRequest().getHeaders().containsKey("Authorization")){
                return handleUnAuthorized(exchange);
            }

            String authorization = exchange.getRequest().getHeaders().get("Authorization").get(0);
            UserToken loginUser = findLoginUser(authorization);

            if(loginUser == null){
                return handleUnAuthorized(exchange);
            }
            //인가 시간이 지난 경우
            if(loginUser.getExpireTime().isBefore(LocalDateTime.now())){
                return handleUnAuthorized(exchange);
            }

            boolean isAdmin = isAdminPath(path) && loginUser.getUser().getAuthorize() == UserAuthorize.ADMIN;
            boolean isUser = isUserPath(path) && loginUser.getUser().getAuthorize() == UserAuthorize.USER;

            if(isAdmin || isUser){
                return chain.filter(exchange).then(Mono.fromRunnable((Runnable) () -> {
                    if(config.isPreLogger()){
                        log.info("Authorization Filter end :request id -> {}", request.getId());
                    }
                }));
            }

            return handleUnAuthorized(exchange);
        };
    }

    private Mono<Void> handleUnAuthorized(ServerWebExchange exchange){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }
    @Getter
    static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;

        public Config(String baseMessage, boolean preLogger, boolean postLogger) {
            this.baseMessage = baseMessage;
            this.preLogger = preLogger;
            this.postLogger = postLogger;
        }
    }


}
