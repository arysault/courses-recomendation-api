package org.challengegroup.coursesrecomendation.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InternalJwtProvider {

    @Value("${internal.jwt.secret}")
    private String internalSecret;

    @Value("${internal.jwt.expiration}")
    private long internalExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = internalSecret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");  
    }

    public String generateInternalToken() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + internalExpiration);

        return Jwts.builder()
                .subject("spring-boot-service")
                .claim("service", "couse-recommendation")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey(), Jwts.SIG.HS256) 
                .compact();
    }
}
