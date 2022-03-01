package com.example.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 *
 * api gateway 인가 : https://velog.io/@tlatldms/API-Gateway-Refresh-JWT-%EC%9D%B8%EC%A6%9D%EC%84%9C%EB%B2%84-%EA%B5%AC%EC%B6%95%ED%95%98%EA%B8%B0-Spring-boot-Spring-Cloud-Gateway-Redis-mysql-JPA-2%ED%8E%B8
 * https://www.baeldung.com/spring-cloud-custom-gateway-filters
 * 선행 학습
 * 리액터 프로그래밍, 논블로킹,블로킹 동기,비동기
 * 논블로킹, 비동기 방식으로 인해 Tomcat 서버가 아닌 Netty서버를 사용
 *
 * 역할
 * 인증 및 권한 부여
 * 라우팅, 클라이언트 요청에 대한 엔드 포인트 통일화
 * 단일 모니터링, 로깅
 * 필터를 이용한 요청,응답 변환기
 * 응답 캐싱 기능이 존재
 * 부하 분산 (로드 밸런싱)
 *
 * 알아야 할 단어
 * 라우트(Route): Url와 Predicate라는 조건들의 목록 그리고 필터들을 이용하여 어디에 라우팅 할것인지
 * 조건자(Predicates): 각 요청을 처리하기 전에 실행되는 로직, 헤더와 입력값 등 다양한 HTTP 요청이 정의된 기준에 맞는지를 확인
 * 필터(Filter) :  요청과 응답, Request, Response을 특정 필터를 타게 함으로 우리가 원하는 방식으로 요청을 보내거나 헤더를 조작할 수 있고, 해당 필터를 이용해서 로그 파일을 작성
 * - Pre Filter : 특정 작업이 일어나기 전에 과정
 * - Post Filter : 특정 작업이 끝난 후에 지정
 *
 * 가정 로그인 서버와 유저 정보 서버 해보기
 * - 인증 및 권한 부여
 * - 로깅 및 모니터링
 * - 응답 캐싱 확인
 * - 엔드포인트 통일화
 * - 응답 헤더 변경
 * - 부하 분산
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
