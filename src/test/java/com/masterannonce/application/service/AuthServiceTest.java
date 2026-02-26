package com.masterannonce.application.service;

import com.masterannonce.application.dto.LoginRequest;
import com.masterannonce.application.dto.LoginResponse;
import com.masterannonce.application.dto.RegisterRequest;
import com.masterannonce.domain.exception.BusinessException;
import com.masterannonce.domain.model.Role;
import com.masterannonce.domain.model.User;
import com.masterannonce.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthService — login, register, refresh token.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@email.com", "encodedPassword");
        testUser.setId(1L);
        testUser.setRole(Role.ROLE_USER);
    }

    // ===== LOGIN =====

    @Test
    @DisplayName("login — succès avec identifiants valides")
    void login_success() {
        LoginRequest request = new LoginRequest("testuser", "Password1");

        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("Password1", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(1L, "testuser", "ROLE_USER")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(1L, "testuser")).thenReturn("refresh-token");

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.role()).isEqualTo("ROLE_USER");

        verify(userService).getUserByUsername("testuser");
        verify(passwordEncoder).matches("Password1", "encodedPassword");
    }

    @Test
    @DisplayName("login — lève BusinessException si le username n'existe pas")
    void login_unknownUsername_throwsBusinessException() {
        LoginRequest request = new LoginRequest("inconnu", "Password1");

        when(userService.getUserByUsername("inconnu")).thenThrow(new RuntimeException("Non trouvé"));

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Identifiants invalides");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login — lève BusinessException si le mot de passe est incorrect")
    void login_wrongPassword_throwsBusinessException() {
        LoginRequest request = new LoginRequest("testuser", "MauvaisMotDePasse");

        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("MauvaisMotDePasse", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Identifiants invalides");

        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString());
    }

    // ===== REGISTER =====

    @Test
    @DisplayName("register — succès, retourne un JWT pour le nouvel utilisateur")
    void register_success() {
        RegisterRequest request = new RegisterRequest("newuser", "new@email.com", "Password1");

        User savedUser = new User("newuser", "new@email.com", "encodedPassword");
        savedUser.setId(2L);
        savedUser.setRole(Role.ROLE_USER);

        when(userService.register(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(2L, "newuser", "ROLE_USER")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(2L, "newuser")).thenReturn("refresh-token");

        LoginResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("access-token");
        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.userId()).isEqualTo(2L);

        verify(userService).register(any(User.class));
    }

    @Test
    @DisplayName("register — lève BusinessException si le username est déjà pris")
    void register_duplicateUsername_throwsBusinessException() {
        RegisterRequest request = new RegisterRequest("testuser", "other@email.com", "Password1");

        when(userService.register(any(User.class)))
            .thenThrow(new BusinessException("Le nom d'utilisateur est déjà pris"));

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("déjà pris");
    }

    // ===== REFRESH TOKEN =====

    @Test
    @DisplayName("refreshToken — succès avec un refresh token valide")
    void refreshToken_success() {
        when(jwtService.isRefreshToken("valid-refresh")).thenReturn(true);
        when(jwtService.getUsernameFromToken("valid-refresh")).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.generateToken(1L, "testuser", "ROLE_USER")).thenReturn("new-access");
        when(jwtService.generateRefreshToken(1L, "testuser")).thenReturn("new-refresh");

        LoginResponse response = authService.refreshToken("valid-refresh");

        assertThat(response.token()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");

        verify(jwtService).isRefreshToken("valid-refresh");
    }

    @Test
    @DisplayName("refreshToken — lève BusinessException si ce n'est pas un refresh token")
    void refreshToken_notRefreshToken_throwsBusinessException() {
        when(jwtService.isRefreshToken("access-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("access-token"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("refresh token");

        verify(jwtService, never()).getUsernameFromToken(anyString());
    }
}
