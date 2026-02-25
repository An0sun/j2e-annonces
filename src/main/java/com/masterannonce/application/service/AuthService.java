package com.masterannonce.application.service;

import com.masterannonce.application.dto.LoginRequest;
import com.masterannonce.application.dto.LoginResponse;
import com.masterannonce.application.dto.RegisterRequest;
import com.masterannonce.domain.exception.BusinessException;
import com.masterannonce.domain.model.User;
import com.masterannonce.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'authentification : login, register, refresh token.
 */
@Service
@Transactional
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authentification : vérifie les credentials et retourne un JWT.
     */
    public LoginResponse login(LoginRequest request) {
        User user;
        try {
            user = userService.getUserByUsername(request.username());
        } catch (Exception e) {
            throw new BusinessException("Identifiants invalides");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("Identifiants invalides");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername());

        return new LoginResponse(token, refreshToken, user.getUsername(), user.getId(), user.getRole().name());
    }

    /**
     * Inscription : crée un nouvel utilisateur.
     */
    public LoginResponse register(RegisterRequest request) {
        User user = new User(request.username(), request.email(), request.password());
        User saved = userService.register(user);

        String token = jwtService.generateToken(saved.getId(), saved.getUsername(), saved.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(saved.getId(), saved.getUsername());

        return new LoginResponse(token, refreshToken, saved.getUsername(), saved.getId(), saved.getRole().name());
    }

    /**
     * Rafraîchissement du token : retourne un nouveau couple access + refresh.
     */
    public LoginResponse refreshToken(String refreshTokenValue) {
        if (!jwtService.isRefreshToken(refreshTokenValue)) {
            throw new BusinessException("Le token fourni n'est pas un refresh token valide");
        }

        String username = jwtService.getUsernameFromToken(refreshTokenValue);
        Long userId = jwtService.getUserIdFromToken(refreshTokenValue);

        User user = userService.getUserByUsername(username);

        String newToken = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        String newRefresh = jwtService.generateRefreshToken(user.getId(), user.getUsername());

        return new LoginResponse(newToken, newRefresh, user.getUsername(), user.getId(), user.getRole().name());
    }
}
