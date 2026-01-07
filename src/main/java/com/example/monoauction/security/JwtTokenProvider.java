package com.example.monoauction.security;



import com.example.monoauction.user.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKeySpec SECRET_KEY;
    private final long EXPIRATION_TIME;
    private final long REFRESH_EXPIRATION_TIME;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretString,
                            @Value("${jwt.expiration}") long expiration,
                            @Value("${jwt.refresh-expiration}") long refreshExpiration) {

        this.SECRET_KEY = new SecretKeySpec(Base64.getDecoder().decode(secretString), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.EXPIRATION_TIME = expiration;
        this.REFRESH_EXPIRATION_TIME = refreshExpiration;

    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        return createToken(claims, user, EXPIRATION_TIME);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user, REFRESH_EXPIRATION_TIME);
    }

    private String createToken(Map<String, Object> claims, User user, long expirationTime) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claims(claims)
                .issuer("AuctionHouse")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY)
                .compact();
    }

    public Long getUserIdFromToken(String token){
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public long getAccessTokenExpirationInMillis() {
        return EXPIRATION_TIME;
    }

    public long getRefreshTokenExpirationInMillis() {
        return REFRESH_EXPIRATION_TIME;
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (SecurityException ex) {
            log.error("Invalid JWT Signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT Token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT Token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT Token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT Claims String Is Empty");
        }
        return false;
    }

}
