package com.example.loginservice.security.authentication.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class JwtUtils {

    private static final String SECRET_KEY = "ebb0f9f6-ab3b-4664-8eae-6cd7e74406cf";
    private static final int EXPIRE_SECOND = 24 * 60 * 60; // 하루

    //jwt_token생성
    public static String createJwtToken(String username) {

        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, EXPIRE_SECOND);

        Claims claims = new DefaultClaims();
        claims.setSubject(username);
        claims.setId(UUID.randomUUID().toString());
        claims.setExpiration(calendar.getTime());

        byte[] secretByte = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key key = new SecretKeySpec(secretByte, SignatureAlgorithm.HS256.getJcaName());

        JwtBuilder builder = Jwts.builder()
                .setHeader(header)
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, key);
        return builder.compact();
    }

    public static Claims getClaimByToken(String token){
        return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                .parseClaimsJws(token).getBody();
    }

    public static LocalDateTime getExpireTimeByToken(String token){
        Claims claims = getClaimByToken(token);
        Date date = claims.getExpiration();

        return date.toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }


}
