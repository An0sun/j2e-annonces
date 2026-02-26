package com.masterannonce.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterannonce.application.dto.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limiteur de débit pour les endpoints d'authentification.
 * Limite les tentatives de connexion à MAX_ATTEMPTS par IP dans la fenêtre de temps.
 * Utilise une approche simple de fenêtre glissante en mémoire.
 * S'applique uniquement à POST /api/v1/auth/login.
 * Désactivé pendant les tests via @Profile("!test").
 */
@Component
@Profile("!test")
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000; // 1 minute (fenêtre de temps)

    private final Map<String, RateWindow> attempts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String ip = getClientIp(request);
        RateWindow window = attempts.computeIfAbsent(ip, k -> new RateWindow());

        // Réinitialiser la fenêtre si expirée
        long now = System.currentTimeMillis();
        if (now - window.windowStart > WINDOW_MS) {
            window.reset(now);
        }

        if (window.count.incrementAndGet() > MAX_ATTEMPTS) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiErrorResponse error = new ApiErrorResponse(429, "Too Many Requests",
                "Trop de tentatives de connexion. Réessayez dans 1 minute.");
            objectMapper.writeValue(response.getOutputStream(), error);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Limiter uniquement POST /api/v1/auth/login
        return !(request.getRequestURI().equals("/api/v1/auth/login")
                && "POST".equalsIgnoreCase(request.getMethod()));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateWindow {
        volatile long windowStart = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);

        void reset(long now) {
            this.windowStart = now;
            this.count.set(0);
        }
    }
}
