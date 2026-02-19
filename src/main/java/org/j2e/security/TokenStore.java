package org.j2e.security;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stockage en mémoire des tokens d'authentification.
 * Singleton thread-safe pour gérer les tokens stateless.
 *
 * Note : en production, on utiliserait JWT ou un cache distribué (Redis).
 * Ici, le stockage mémoire est suffisant pour le contexte du TP.
 */
public class TokenStore {

    private static final TokenStore INSTANCE = new TokenStore();
    private static final long TOKEN_EXPIRATION_SECONDS = 3600; // 1 heure

    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    private TokenStore() {
    }

    public static TokenStore getInstance() {
        return INSTANCE;
    }

    /**
     * Générer et stocker un nouveau token pour un utilisateur.
     */
    public String generateToken(Long userId, String username) {
        String token = UUID.randomUUID().toString();
        Instant expiration = Instant.now().plusSeconds(TOKEN_EXPIRATION_SECONDS);
        tokens.put(token, new TokenInfo(userId, username, expiration));
        return token;
    }

    /**
     * Valider un token et retourner les infos associées.
     * 
     * @return TokenInfo si le token est valide et non expiré, null sinon.
     */
    public TokenInfo validateToken(String token) {
        if (token == null)
            return null;

        TokenInfo info = tokens.get(token);
        if (info == null)
            return null;

        // Vérifier l'expiration
        if (Instant.now().isAfter(info.getExpiration())) {
            tokens.remove(token); // Nettoyage du token expiré
            return null;
        }

        return info;
    }

    /**
     * Invalider un token (logout).
     */
    public void removeToken(String token) {
        tokens.remove(token);
    }

    /**
     * Informations associées à un token.
     */
    public static class TokenInfo {
        private final Long userId;
        private final String username;
        private final Instant expiration;

        public TokenInfo(Long userId, String username, Instant expiration) {
            this.userId = userId;
            this.username = username;
            this.expiration = expiration;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public Instant getExpiration() {
            return expiration;
        }
    }
}
