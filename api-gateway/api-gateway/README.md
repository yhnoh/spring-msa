# Spring Cloud Gateway

# [Notion](https://superb-ermine-a50.notion.site/Spring-Cloud-Gateway-5937d80f800b43d685feaff65b33a7a0)

### 목표

- 어떻게 작동하는지?
- 왜 tomcat을 이용하지 않고, netty를 이용하는가?
- 어떤 역할을 하는지?
- 숙지하면 좋을 용어
- 시나리오를 짜서 구현을 해보자

### 동작원리

1. 클라이언트는 Spring Cloud Gateway에게 요청
2. **Gateway의 경로(엔드포인트, Route)와 일치시,** Gateway Handler Mapping이 Gateway Web Handler에게 처리 요청
3. “사전” 필터가 실행 된 이후(요청, 응답에 대한 변환이 가능), 프록시 서비스(실제 수행을 하는 어플리케이션)에게 요청 및 응답을 받은 이후, “포스트”필터(요청, 응답에 대한 변환이 가능)이 실행
4. Spring Cloud Gateway는 클라이언트에게 응답



### webflux를 사용하는 이유

- 흩어져 있는 서비스들을, 중앙에서 처리할 수 있게 하는 하나의 통로와 같은 역할을한다.
- 만약 트래픽이 몰릴 때, 동기/블로킹 방식의 서버를 이용하게 될 경우,  Spring Cloud Gateway가 다른 요청에 대한 처리를 못하게 될것이다.
- 이와 같은 방식을 해결하고자, 비동기/논블로킹 방식인 Netty 서버를 사용한다.

### 역할

- 인증 및 권한 부여
- 라우팅 및 엔드 포인트 통일화
- 단일 모니터링, 로깅
- 응답 캐싱
- 부하 분산
- 필터를 이용한 요청, 응답 변환
- 등등....

### 용어

- **라우트(Route)** : 한마디로 경로, **대상 URL + 조건자 + 필터들로 이루어져 있다.**
- **조건자(Predicates)** : 각 요청을 처리하기 전에 실행되는 로직, 헤더와 입력값 등 다양한 HTTP 요청이 정의된 기준에 맞는지를 확인
- **필터(Filter)** :  요청과 응답, Request, Response을 특정 필터를 타게 함으로 우리가 원하는 방식으로 요청을 보내거나 헤더를 조작할 수 있고, 해당 필터를 이용해서 로그 파일을 작성
    - Pre Filter : 특정 작업이 일어나기 전에 과정
    - Post Filter : 특정 작업이 끝난 후에 지정

### 시나리오

- h2 로컬 데이터 베이스 사용, 인메모리가 아닌 중앙 서버 이용
- 로그인 서비스, 유저 서비스 구현하고 이를 Spring Cloud Gateway에 연결
- Spring Cloud Gateway
    1. 모든 요청에 대한 필터를 만든다. (`GlobalFilter`)
    2. 유저 서비스와 로그인 서비스를 등록해 준다.
    3. 로그인을 한 이후 유저 서비스에서 정보를 보기 위해서는 인가를 하도록 만든다. (`AuthorizationFilter`)
    4. 유저 서비스는 부하 분산이 가능하도록 구축한다.
    5. 모든 요청에 대한 로그 정보를 남긴다.
- 로그인 서비스
    1. POST, /login 요청만 가능
    2. JWT를 이용하여 H2데이터 베이스에 토큰 정보 저장
- 유저 서비스
    1. /user-service/**요청을 유저서비스로 요청할 때는  /**로 변경
    2. /users/{username}는 USER권한, /admins/{username}는 ADMIN권한

### Spring Cloud Gateway

application.yml

```yaml
spring:
  application:
    name: api-gateway
#H2데이터 베이스 사용
datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:tcp://127.0.0.1/~/api-gateway
    username: sa
    password: 1234
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
  cloud:
    gateway:
#글로벌 필터 :모든 필터에서 가장 최 상단에 위치
default-filters:
        - name: GlobalFilter
          args:
            baseMessage: Spring Cloud Gateway Filter
            preLogger: true
            postLogger: true
      routes:
#로그인 서비스
#대상 URL은 http://127.0.0.1:9000번 포트이다. http://127.0.0.1:8080 -> http://127.0.0.1:9000
        # /login와 POST방식만 요청 가능
- id: login-service
          uri: http://127.0.0.1:9000
          predicates:
            - Path=/login
            - Method=POST
#유저 서비스
# /user-service/** -> /user-service/**
        # Weight=group-user, 5부하 분산을 사용하기 위해서 user-service1에 5, user-service2에 5
        # RewritePath=/user-service/(?<path>.*),/$\{path}, user-service/** -> /**로 변경
- id: user-service1
          uri: http://127.0.0.1:8500
          predicates:
           - Path=/user-service/**
           - Weight=group-user, 5
          filters:
           - name: AuthorizationFilter
             args:
               baseMessage: Spring Cloud Gateway Filter
               preLogger: true
               postLogger: true
           - RewritePath=/user-service/(?<path>.*),/$\{path}
        - id: user-service2
          uri: http://127.0.0.1:8501
          predicates:
            - Path=/user-service/**
            - Weight=group-user, 5
          filters:
            - name: AuthorizationFilter
              args:
                baseMessage: Spring Cloud Gateway Filter
                preLogger: true
                postLogger: true
            - RewritePath=/user-service/(?<path>.*),/$\{path}

```

GlobalFilter

```java
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

```

AuthorizationFilter

```java
@Slf4j
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<Config> {
    private final UserTokenJpaRepository userTokenJpaRepository;
    AntPathMatcher antPathMatcher = new AntPathMatcher();

    //토큰 정보를 받아서, 유저정보 찾기
    @Transactional(readOnly = true)
    UserToken findLoginUser(String token){
        UserToken userToken = userTokenJpaRepository.findByToken(token);
        return userToken;
    }

    //USER PATH 체크
    public boolean isUserPath(String path){
        return antPathMatcher.match("/user-service/users/**", path);
    }
    //ADMIN PATH 체크
    public boolean isAdminPath(String path){
        return antPathMatcher.match("/user-service/admins/**", path);
    }
    //권한이 필요한지 체크
    public boolean isAuthorizationPath(String path){
        return isUserPath(path) || isAdminPath(path);
    }

    public AuthorizationFilter(UserTokenJpaRepository userTokenJpaRepository) {
        super(Config.class);
        this.userTokenJpaRepository = userTokenJpaRepository;
    }

    private Mono<Void> handleUnAuthorized(ServerWebExchange exchange){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
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
                }));
            }

            return handleUnAuthorized(exchange);
        };
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

```

access_log : 스프링부트 시스템이 아니기 때문에 설정 필요

```java
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        //로깅
        System.setProperty("reactor.netty.http.server.accessLogEnabled", "true");
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
```

> *[https://cloud.spring.io/spring-cloud-gateway/reference/html](https://cloud.spring.io/spring-cloud-gateway/reference/html/)*
>

> [*https://cheese10yun.github.io/spring-cloud-gateway*](https://cheese10yun.github.io/spring-cloud-gateway/)
>