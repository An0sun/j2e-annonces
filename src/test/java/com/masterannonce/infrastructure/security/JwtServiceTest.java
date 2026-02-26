package com.masterannonce.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour JwtService — génération, validation et extraction de tokens.
 */
class JwtServiceTest {

    private static final String SECRET = "dGhpc0lzQVZlcnlMb25nU2VjcmV0S2V5Rm9ySldUU2lnbmluZ1RoYXRJc0F0TGVhc3Q1MTJCaXRzTG9uZ0ZvckhTNTEyQWxnb3JpdGhtQ29tcGxpYW5jZSEh";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpirationMs(3600000);       // 1h
        props.setRefreshExpirationMs(86400000); // 24h
        jwtService = new JwtService(props);
    }

    @Test
    @DisplayName("generateToken — contient les claims attendus (userId, username, role)")
    void generateToken_containsExpectedClaims() {
        String token = jwtService.generateToken(42L, "testuser", "ROLE_USER");

        Claims claims = jwtService.validateToken(token);
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("generateRefreshToken — contient le claim type=refresh")
    void generateRefreshToken_containsTypeClaim() {
        String token = jwtService.generateRefreshToken(42L, "testuser");

        Claims claims = jwtService.validateToken(token);
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("type", String.class)).isEqualTo("refresh");
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
    }

    @Test
    @DisplayName("isRefreshToken — vrai pour refresh, faux pour access")
    void isRefreshToken_distinguishesTokenTypes() {
        String access = jwtService.generateToken(1L, "user", "ROLE_USER");
        String refresh = jwtService.generateRefreshToken(1L, "user");

        assertThat(jwtService.isRefreshToken(refresh)).isTrue();
        assertThat(jwtService.isRefreshToken(access)).isFalse();
    }

    @Test
    @DisplayName("validateToken — lève ExpiredJwtException pour un token expiré")
    void validateToken_throwsOnExpired() {
        // Créer un service avec une expiration très courte
        JwtProperties shortProps = new JwtProperties();
        shortProps.setSecret(SECRET);
        shortProps.setExpirationMs(-1000); // already expired
        shortProps.setRefreshExpirationMs(86400000);
        JwtService shortService = new JwtService(shortProps);

        String token = shortService.generateToken(1L, "user", "ROLE_USER");

        assertThatThrownBy(() -> shortService.validateToken(token))
            .isInstanceOf(JwtException.class)
            .hasMessageContaining("expiré");
    }

    @Test
    @DisplayName("validateToken — lève JwtException pour une signature invalide")
    void validateToken_throwsOnInvalidSignature() {
        // Générer un token avec une clé différente
        SecretKey otherKey = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode("b3RoZXJTZWNyZXRLZXlUaGF0SXNBbHNvTG9uZ0Vub3VnaEZvckpXVFNpZ25pbmdUaGF0SXNBdExlYXN0NTEyQml0cw=="));
        String fakeToken = Jwts.builder()
            .subject("hacker")
            .signWith(otherKey)
            .compact();

        assertThatThrownBy(() -> jwtService.validateToken(fakeToken))
            .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("getUsernameFromToken / getUserIdFromToken / getRoleFromToken — extraction correcte")
    void extractionMethods() {
        String token = jwtService.generateToken(99L, "admin", "ROLE_ADMIN");

        assertThat(jwtService.getUsernameFromToken(token)).isEqualTo("admin");
        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo(99L);
        assertThat(jwtService.getRoleFromToken(token)).isEqualTo("ROLE_ADMIN");
    }
}
