package com.bank.application.auth_app.SecurityConfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey = Keys.hmacShaKeyFor("supersecure256bitkeyfortestsupersecure256bitkeyfortests".getBytes());
    private long validityInMilliseconds = 300000;

    public String generateToken(String username) {
        long expirationTime = 1000 * 60 * 60 * 10;

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", List.of("ROLE_UZIVATEL", "ROLE_PRACOVNIK"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }
}
