package com.example.apigateway.filter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.example.apigateway.filter.GlobalFilter.*;

@Slf4j
@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<Config> {

    public GlobalFilter() {
        super(Config.class);
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            if(config.isPreLogger()){
                log.info("Global filter start :request id -> {} threadName = {}", request.getId(), Thread.currentThread().getName());
            }
            return chain.filter(exchange).then(Mono.fromRunnable((Runnable) () -> {
                if(config.isPostLogger()){
                    log.info("Global filter end :request id -> {} threadName = {}", request.getId(), Thread.currentThread().getName());
                }
            }));
        };
    }

    //Config는 configuration 정보를 담을 수 있음
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
