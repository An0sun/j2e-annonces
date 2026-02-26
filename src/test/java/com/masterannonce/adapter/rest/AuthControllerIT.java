package com.masterannonce.adapter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterannonce.AbstractIntegrationTest;
import com.masterannonce.application.dto.LoginRequest;
import com.masterannonce.application.dto.LoginResponse;
import com.masterannonce.application.dto.RegisterRequest;
import com.masterannonce.domain.model.Role;
import com.masterannonce.domain.model.User;
import com.masterannonce.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration REST pour l'authentification.
 * Utilise Testcontainers PostgreSQL via AbstractIntegrationTest.
 */
@AutoConfigureMockMvc
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Créer un utilisateur de test
        User user = new User("testuser", "test@test.com", passwordEncoder.encode("Password1"));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — Login réussi retourne un JWT")
    void loginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "Password1");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.username").value("testuser"))
            .andReturn();

        LoginResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(), LoginResponse.class);
        assertThat(response.token()).isNotBlank();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — Identifiants invalides retourne 400")
    void loginBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register — Inscription réussie")
    void registerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "newuser@test.com", "Password1A");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register — Duplicate username retourne 400")
    void registerDuplicateUsername() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "other@test.com", "Password1");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh — Rafraîchir le token")
    void refreshToken() throws Exception {
        // D'abord, obtenir un refresh token via login
        LoginRequest loginReq = new LoginRequest("testuser", "Password1");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
            loginResult.getResponse().getContentAsString(), LoginResponse.class);

        // Utiliser le refresh token
        String body = "{\"refreshToken\": \"" + loginResponse.refreshToken() + "\"}";

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
