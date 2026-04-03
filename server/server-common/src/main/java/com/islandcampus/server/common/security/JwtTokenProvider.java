package com.islandcampus.server.common.security;

import com.islandcampus.server.common.constant.Constants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long tokenExpireMs;
    private final Map<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    public JwtTokenProvider(@Value("${jwt.secret:island-campus-jwt-secret-key-2024-super-long-secret-for-hmac-sha256!!}") String secret,
                            @Value("${jwt.expire-ms:${Constants.TOKEN_EXPIRE_MS}}") long expireMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenExpireMs = expireMs > 0 ? expireMs : Constants.TOKEN_EXPIRE_MS;
    }

    public String generateToken(Long userId, String username, Long roleId, Long orgId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenExpireMs);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roleId", roleId)
                .claim("orgId", orgId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + Constants.REFRESH_TOKEN_EXPIRE_DAYS * 86400000L);
        return Jwts.builder()
                .subject("refresh:" + userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        if (isBlacklisted(token)) {
            throw new JwtException("Token已失效");
        }
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void blacklist(String token) {
        try {
            Claims claims = parseToken(token);
            tokenBlacklist.put(token, claims.getExpiration().getTime());
        } catch (Exception ignored) {}
    }

    public boolean isBlacklisted(String token) {
        Long expiry = tokenBlacklist.get(token);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            tokenBlacklist.remove(token);
            return false;
        }
        return true;
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    public Long getRoleId(String token) {
        Object val = parseToken(token).get("roleId");
        return val != null ? ((Number) val).longValue() : null;
    }

    public Long getOrgId(String token) {
        Object val = parseToken(token).get("orgId");
        return val != null ? ((Number) val).longValue() : null;
    }
}
