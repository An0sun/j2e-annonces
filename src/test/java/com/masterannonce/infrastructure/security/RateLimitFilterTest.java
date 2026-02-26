package com.masterannonce.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private ObjectMapper objectMapper;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        filter = new RateLimitFilter(objectMapper);
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("shouldNotFilter — retourne true pour GET /api/v1/annonces")
    void shouldNotFilter_nonLoginEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/annonces");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    @DisplayName("shouldNotFilter — retourne false pour POST /api/v1/auth/login")
    void shouldNotFilter_loginEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    @DisplayName("shouldNotFilter — retourne true pour GET /api/v1/auth/login")
    void shouldNotFilter_getLoginEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    @DisplayName("doFilterInternal — laisse passer les requêtes sous la limite")
    void doFilterInternal_allowsUnderLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("doFilterInternal — bloque après 5 tentatives (429)")
    void doFilterInternal_blocksAfterLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("10.0.0.1");

        // 5 tentatives autorisées
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            filter.doFilterInternal(request, resp, filterChain);
            assertThat(resp.getStatus()).isEqualTo(200);
        }

        // 6ème tentative bloquée
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, blockedResponse, filterChain);

        assertThat(blockedResponse.getStatus()).isEqualTo(429);
        assertThat(blockedResponse.getContentAsString()).contains("Too Many Requests");
    }

    @Test
    @DisplayName("doFilterInternal — utilise X-Forwarded-For si présent")
    void doFilterInternal_usesXForwardedFor() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal — IP différentes ont des compteurs séparés")
    void doFilterInternal_separateCountersPerIp() throws ServletException, IOException {
        // Épuiser la limite pour IP1
        for (int i = 0; i < 6; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setRemoteAddr("1.1.1.1");
            filter.doFilterInternal(req, new MockHttpServletResponse(), filterChain);
        }

        // IP2 devrait encore pouvoir passer
        MockHttpServletRequest req2 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req2.setRemoteAddr("2.2.2.2");
        MockHttpServletResponse resp2 = new MockHttpServletResponse();
        filter.doFilterInternal(req2, resp2, filterChain);

        assertThat(resp2.getStatus()).isEqualTo(200);
    }
}
