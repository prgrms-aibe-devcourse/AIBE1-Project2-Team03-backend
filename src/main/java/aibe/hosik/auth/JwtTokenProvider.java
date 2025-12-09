package aibe.hosik.auth;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Instant now = Instant.now();
        Date expiration = new Date(now.toEpochMilli() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(expiration)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        Date expiration = new Date(now.toEpochMilli() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(expiration)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String getUsername(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (MalformedJwtException | UnsupportedJwtException | SignatureException e) {
            log.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있거나 null입니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.JWT_TOKEN_NOT_FOUND);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (MalformedJwtException | UnsupportedJwtException | SignatureException e) {
            log.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있거나 null입니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.JWT_TOKEN_NOT_FOUND);
        } catch (JwtException e) {
            log.warn("JWT 처리 중 오류가 발생했습니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
        }
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        try {
            UserDetails user = new User(getUsername(token), "", Collections.emptyList());
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        } catch (CustomException e) {
            // 이미 적절한 CustomException이 발생했으므로 그대로 재발생
            throw e;
        } catch (Exception e) {
            log.error("인증 정보 생성 중 오류가 발생했습니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }
}