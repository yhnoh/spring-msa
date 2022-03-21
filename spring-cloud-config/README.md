# Spring Cloud Config

### Notion : [https://superb-ermine-a50.notion.site/Spring-Cloud-Config-8139066e26e04ee990909ad8d3d2b653](https://superb-ermine-a50.notion.site/Spring-Cloud-Config-8139066e26e04ee990909ad8d3d2b653)

## Spring Cloud Config

### Spring Cloud Config의 역할

- 프로젝트 **설정 파일에 대한 값을 변경할 때마다 다시 빌드후 배포하는 과정이 필요**
- 위 과정을 없애기 위해 **설정이 바뀔때마다 빌드와 배포가 필요 없는 방식이 필요**
- **설정을 위한 별도의 서버를 구성**하고, **실행중인 애플리케이션이 서버에서 설정 정보를 받아와 갱신**하는 방식

### Spring Cloud Config HTTP 엔드 포인트 형식

```
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```

- confit-server에서 application은 **spring.application.name**으로 주입
- config-client에서 application은 **spring.config.name으로 주입**
- profile은 활성화된 프로파일
- label은 option이고 branch명, 기본적으로 master로 설정

### Config Server 설정

- **pom.xml**
    
    ```xml
    <dependencies>
    ...
    	<dependency>
    		<groupId>org.springframework.cloud</groupId>
    		<artifactId>spring-cloud-config-server</artifactId>
    	</dependency>
    ...
    </dependencies>
    ```
    
- **프로젝트 Main파일**
    
    `@EnableConfigServer` 어노테이션 선언
    
    ```java
    @EnableConfigServer
    @SpringBootApplication
    public class ConfigServerApplication {
    
        public static void main(String[] args) {
            SpringApplication.run(ConfigServerApplication.class, args);
        }
    
    }
    ```
    

### Git 리포지토리 로컬 파일 시스템 이용

- **application.yml**
    - window의 경우 file:///아닌경우 file://
    
    ```yaml
    server:
      port: 8888
    
    spring:
      cloud:
        config:
          server:
            git:
              uri: file:///${user.home}/Desktop/yhnoh/study/msa-config-property
    ```
    
- 로컬 파일
    
    ```bash
    [폴더위치]
    C:\Users\yhnoh\Desktop\yhnoh\study\msa\spring-cloud-config\config-property
    [파일추가]
    application.yml
    
    [Git명령어]
    git init
    git add ./
    git commit -m "message"
    
    [엔드포인트]
    http://localhost:8888/application/defalut
    ```
    

### Git 리포지토리 원격저장소 이용

- **application.yml**
    
    ```yaml
    server:
      port: 8888
    
    spring:
      cloud:
        config:
          server:
            git:
              uri: https://github.com/yhnoh/spring-msa-config-property
    ```
    

### Config Client 설정

- **application.yml**
    
    ```yaml
    server:
      port: 8889
    
    spring:
    	config:
        import: "optional:configserver:http://localhost:8888"
    management:
      endpoints:
        web:
          exposure:
            include: refresh
    
    ---
    server:
      port: 8890
    
    spring:
      config:
        activate:
          on-profile: dev
    
    ---
    server:
      port: 8891
    
    spring:
      config:
        activate:
          on-profile: real
    ```
    
- **pom.xml**
    
    ```xml
    <dependencies>
    ...
    	<dependency>
    		<groupId>org.springframework.boot</groupId>
    		<artifactId>spring-boot-starter-actuator</artifactId>
    	</dependency>
    	<dependency>
    	    <groupId>org.springframework.boot</groupId>
    	    <artifactId>spring-boot-starter-web</artifactId>
    	</dependency>
    	<dependency>
    	    <groupId>org.springframework.cloud</groupId>
    	    <artifactId>spring-cloud-starter-config</artifactId>
    	</dependency>
    ...
    </dependencies>
    ```
    
- **Config Client `actuator/refresh`를 이용하여 변경사항 반영**
    - 사용자가 설정 파일을 변경하고 Git 저장소를 업데이트 했다면
    클라이언트에 `actuator/refresh` 엔드 포인트를 호출하여 설정값을 변경한다.
    - 설정값 갱신을 위한 `actuator/refresh`엔드 포인트 호출은 각각의 클라어인트를 호출해야 반영이 가능

### Spring Cloud Config `**actuator/refresh` 이용한** 구조



- 사용자가 설정파일을 변경하고 Git저장소에 push를 한다.
- 하나의 config client에게 `actuator/refresh`를 요청한다.
- config sever는 갱신된 설정파일을 config client에게 전달한다.
- 나머지 config client에게도 동일한 요청을 하여 설정파일을 갱신한다.

## Spring Cloud Bus

### Spring Cloud Bus의 역할

- 앞에서 Spring Colud Config를 이용하여 스프링 설정이 바뀌었을때 갱신이 가능해 졌다.
- 하지만 설정 정보의 갱신이 필요할 때마다 `actuator/refresh`를 config client에게 **하나 하나씩 호출해야한다는 단점**이 있다.
- 마이크로서비스 환경에서 수많은 config client들이 존재할 것인데, **모든 config client들의 설정정보를 변경하기란 쉽지 않을 것이다.**
- Spring Cloud Bus를 적용 시, 설정정보가 변경될 때마다 연결된 **모든 config client의 정보를 한번에 갱신**시킬 수 있다.

### Config-Client 설정

- **pom.xml**
    
    ```xml
    <dependencies>
    ...
    	<dependency>
    		<groupId>org.springframework.cloud</groupId>
    		<artifactId>spring-cloud-starter-bus-amqp</artifactId>
    	</dependency>
    ...
    </dependencies>
    ```

    - Spring Cloud Bus-amqp는 기본적으로 메시지 브로커인 rabbitmq를 가지고 있다.
    - 메시지 브로커(RabbitMQ, Kafka 등등)을 이용하여 애플리케이션에 이벤트를 전달하는 역할을 한다.
    
- **application.yml**
    
    ```yaml
    server:
      port: 8889
    
    spring:
      rabbitmq:
        username: guest
        password: guest
        host: localhost
        port: 5672
      config:
        import: "optional:configserver:http://localhost:8888"
    management:
      endpoints:
        web:
          exposure:
            include: refresh,bus-refresh
    
    ---
    server:
      port: 8890
    
    spring:
      config:
        activate:
          on-profile: dev
    
    ---
    server:
      port: 8891
    
    spring:
      config:
        activate:
          on-profile: real
    ```
    
- **Config Client `actuator/busrefresh`를 이용하여 변경사항 반영**
    - 사용자가 설정 파일을 변경하고 Git 저장소를 업데이트 했다면
    config client에 `actuator/busrefresh` 엔드 포인트를 호출하여 설정값을 변경한다.
    - **RabbitMQ로 연결된 모든 마이크로서비스의 설정값이 갱신**이 된다.
    

### Spring Cloud Bus `actuator/busrefresh` 이용한 구조


- 사용자가 설정파일을 변경하고 Git저장소에 push를 한다.
- `actuator/busrefresh`를 하나의 config client에게 요청한다.
- **Publish Message** : 요청됨과 동시에 config client는 **메시지 브로커(RabbitMQ, Kafka)에게 설정이 갱신되었다는것을 알린다.**
- 요청을 받은 config client는 설정파일이 갱신된다.
- **Subscribe Message** : 나머지 config client들에게 **설정파일이 변경되었다는 것을 메시지 브로커가 통보를** 한다. 마치 "나머지 어플리케이션에게 `actuator/busrefresh`를 실행시켜줘!!"와 같은 의미를 가진다.
- 나머지 마이크로 서비스들도 설정파일이 갱신이 된다.

## (**Git**) **Webhook을 이용한 자동갱신**

- Spring Cloud bus를 이용하여 각 한번의 엔드포인트 호출로 마이크로서비스들의 설정정보가 변경이 가능했다.
- 하지만 누군가가 엔드포인트 호출을 잊는경우가 생기거나, 아예 **원격저장소에 push를 하자마자 설정정보가 변경이 가능해지면 자동화 구축**이 가능해지지 않을까?
- Webhook을 이용하여 자동화 구축이 가능하다.

### Config-Server 설정

- **pom.xml**
    - **spring-cloud-config-monitor** 의존성은 Git 저장소에서 push 등으로 변경사항이 발생할 때마다 config server가 이벤트를 받을 수 있도록 `/monitor`라는 엔드포인트를 제공한다.
    - ****Spring Cloud Stream**** 의존성은 메시지 주도(message-driven) 또는 이벤트 주도(event-driven)의 마이크로 서비스 개발을 지원한다
    - 메시지 브로커(RabbitMQ, Kafka 등등)을 이용하여 애플리케이션에 이벤트를 전달하는 역할을 한다.
    
    ```xml
    <dependencies>
    ...
    	<dependency>
    	    <groupId>org.springframework.cloud</groupId>
    	    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
    	</dependency>
    	
    	<dependency>
    	    <groupId>org.springframework.cloud</groupId>
    	    <artifactId>spring-cloud-config-monitor</artifactId>
    	</dependency>
    ...
    </dependencies>
    ```
    
- **application.yml**
    
    ```yaml
    server:
      port: 8888
    spring:
      cloud:
        config:
          server:
            git:
              #window? ?? file:/// ???? file://
    #          uri: file:///${user.home}/Desktop/yhnoh/study/msa-config-property
              uri: https://github.com/yhnoh/spring-msa-config-property
            default-label: master
    # spring-cloud-config-monitor 설정
        bus:
          enabled: true
      rabbitmq:
        username: guest
        password: guest
        host: localhost
        port: 5672
    
    #  application:
    #    name: config-service
    ```
    
- **Config Server `actuator/monitor`를 이용하여 변경사항 반영**
    - 사용자가 설정 파일을 변경하고 Git 저장소를 업데이트 했다면
    Config Server에 **`actuator/monitor`** 엔드 포인트를 호출하여 설정값을 변경한다.
    - 로컬 엔드포인트
    
    ```
    POST /monitor HTTP/1.1
    Host: localhost:8888
    Content-Type: application/json
    X-Hook-UUID: webhook-uuid
    X-Event-Key: repo:push
    Content-Length: 23
    
    {"push":{"changes":[]}}
    ```
    

### **Webhook을 이용한** 구조

- 사용자가 Git 원격 저장소에 설정파일 변경 내역을 push
- webhook정보에 지정된 http://{config server host}/monitor 엔드포인트가 호출된 이후, 설정파일 정보가 변경되었다는 것을 메시지 브로커에게 알림
- **Publish Message** : 메시지 브로커의 채널에 설정파일 변경 통보
- S**ubscribe Message** : 채널을 수신하는 각 config client는 변경내용을 수정

> 공식 레퍼런스 : [https://cloud.spring.io/spring-cloud-config/reference/html/](https://cloud.spring.io/spring-cloud-config/reference/html/)
> 

> 공식 레퍼런스 : [https://docs.spring.io/spring-cloud-bus/docs/current/reference/html/](https://docs.spring.io/spring-cloud-bus/docs/current/reference/html/)
> 

> [https://madplay.github.io/post/introduction-to-spring-cloud-config](https://madplay.github.io/post/introduction-to-spring-cloud-config)
> 

> [https://madplay.github.io/post/spring-cloud-bus-example](https://madplay.github.io/post/spring-cloud-bus-example)
> 

> [https://madplay.github.io/post/spring-cloud-config-using-git-webhook-to-auto-refresh](https://madplay.github.io/post/spring-cloud-config-using-git-webhook-to-auto-refresh)
> 

> spring boot 2.4 변경 사항 : [https://multifrontgarden.tistory.com/278](https://multifrontgarden.tistory.com/278)
>
