package com.finance.dashboard.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component // Marks this as a Spring-managed bean so it can be injected anywhere
public class JwtUtil {

    // Reads the secret from application.properties
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration; // Milliseconds (86400000 = 24 hours)

    // Build the signing key from our secret string
    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Create a JWT token for a given email
    // The token contains: who the user is (subject), when it was made, when it expires
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256) // Sign with our secret
                .compact();
    }

    // Extract the email from a token (reverse of above)
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Check if token is valid and not expired
    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            // Token is expired, tampered, or malformed
            return false;
        }
    }
}