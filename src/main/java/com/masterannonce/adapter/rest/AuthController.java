package com.masterannonce.adapter.rest;

import com.masterannonce.application.dto.LoginRequest;
import com.masterannonce.application.dto.LoginResponse;
import com.masterannonce.application.dto.RegisterRequest;
import com.masterannonce.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller d'authentification : login, register, refresh token.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentification", description = "Endpoints d'authentification et d'inscription")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un JWT")
    @ApiResponse(responseCode = "200", description = "Connexion réussie, JWT retourné")
    @ApiResponse(responseCode = "400", description = "Identifiants invalides")
    @ApiResponse(responseCode = "429", description = "Trop de tentatives (rate limit)")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Crée un nouveau compte utilisateur")
    @ApiResponse(responseCode = "201", description = "Compte créé avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides ou username déjà pris")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token", description = "Retourne un nouveau couple access + refresh token")
    @ApiResponse(responseCode = "200", description = "Nouveaux tokens retournés")
    @ApiResponse(responseCode = "400", description = "Refresh token invalide ou manquant")
    public ResponseEntity<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        LoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
