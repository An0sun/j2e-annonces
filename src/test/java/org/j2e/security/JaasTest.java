package org.j2e.security;

import org.junit.jupiter.api.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires JAAS.
 * Exercice 5 Bonus : Tests des LoginModules.
 */
class JaasTest {

    // ===== Tests TokenLoginModule =====

    @Test
    @DisplayName("TokenLoginModule - token valide → Subject peuplé")
    void tokenLoginModule_validToken() throws Exception {
        // Setup : créer un token valide dans le TokenStore
        String token = TokenStore.getInstance().generateToken(42L, "testuser");

        // Créer le LoginModule
        TokenLoginModule module = new TokenLoginModule();
        Subject subject = new Subject();

        CallbackHandler handler = callbacks -> {
            for (Callback cb : callbacks) {
                if (cb instanceof TokenCallback) {
                    ((TokenCallback) cb).setToken(token);
                }
            }
        };

        module.initialize(subject, handler, new HashMap<>(), new HashMap<>());

        // Login + Commit
        assertTrue(module.login());
        assertTrue(module.commit());

        // Vérifier le Subject
        Set<UserPrincipal> users = subject.getPrincipals(UserPrincipal.class);
        assertEquals(1, users.size());
        UserPrincipal principal = users.iterator().next();
        assertEquals(42L, principal.getUserId());
        assertEquals("testuser", principal.getUsername());

        Set<RolePrincipal> roles = subject.getPrincipals(RolePrincipal.class);
        assertEquals(1, roles.size());
        assertEquals(RolePrincipal.ROLE_USER, roles.iterator().next().getRole());

        // Cleanup
        TokenStore.getInstance().removeToken(token);
    }

    @Test
    @DisplayName("TokenLoginModule - token invalide → LoginException")
    void tokenLoginModule_invalidToken() {
        TokenLoginModule module = new TokenLoginModule();
        Subject subject = new Subject();

        CallbackHandler handler = callbacks -> {
            for (Callback cb : callbacks) {
                if (cb instanceof TokenCallback) {
                    ((TokenCallback) cb).setToken("token-bidon-inexistant");
                }
            }
        };

        module.initialize(subject, handler, new HashMap<>(), new HashMap<>());

        assertThrows(LoginException.class, module::login);
    }

    @Test
    @DisplayName("TokenLoginModule - token absent → LoginException")
    void tokenLoginModule_emptyToken() {
        TokenLoginModule module = new TokenLoginModule();
        Subject subject = new Subject();

        CallbackHandler handler = callbacks -> {
            for (Callback cb : callbacks) {
                if (cb instanceof TokenCallback) {
                    ((TokenCallback) cb).setToken("");
                }
            }
        };

        module.initialize(subject, handler, new HashMap<>(), new HashMap<>());

        assertThrows(LoginException.class, module::login);
    }

    @Test
    @DisplayName("TokenLoginModule - token expiré → LoginException")
    void tokenLoginModule_expiredToken() throws Exception {
        // Créer un token puis le supprimer (simule expiration)
        String token = TokenStore.getInstance().generateToken(1L, "expired");
        TokenStore.getInstance().removeToken(token);

        TokenLoginModule module = new TokenLoginModule();
        Subject subject = new Subject();

        CallbackHandler handler = callbacks -> {
            for (Callback cb : callbacks) {
                if (cb instanceof TokenCallback) {
                    ((TokenCallback) cb).setToken(token);
                }
            }
        };

        module.initialize(subject, handler, new HashMap<>(), new HashMap<>());

        assertThrows(LoginException.class, module::login);
    }

    @Test
    @DisplayName("TokenLoginModule - logout nettoie le Subject")
    void tokenLoginModule_logout() throws Exception {
        String token = TokenStore.getInstance().generateToken(10L, "logoutuser");

        TokenLoginModule module = new TokenLoginModule();
        Subject subject = new Subject();

        CallbackHandler handler = callbacks -> {
            for (Callback cb : callbacks) {
                if (cb instanceof TokenCallback) {
                    ((TokenCallback) cb).setToken(token);
                }
            }
        };

        module.initialize(subject, handler, new HashMap<>(), new HashMap<>());
        module.login();
        module.commit();

        // Subject peuplé
        assertFalse(subject.getPrincipals(UserPrincipal.class).isEmpty());

        // Logout
        module.logout();
        assertTrue(subject.getPrincipals(UserPrincipal.class).isEmpty());
        assertTrue(subject.getPrincipals(RolePrincipal.class).isEmpty());

        // Cleanup
        TokenStore.getInstance().removeToken(token);
    }

    // ===== Tests Principals =====

    @Test
    @DisplayName("UserPrincipal - getName retourne le username")
    void userPrincipal_getName() {
        UserPrincipal p = new UserPrincipal(1L, "jean");
        assertEquals("jean", p.getName());
        assertEquals(1L, p.getUserId());
    }

    @Test
    @DisplayName("UserPrincipal - equals et hashCode")
    void userPrincipal_equality() {
        UserPrincipal p1 = new UserPrincipal(1L, "jean");
        UserPrincipal p2 = new UserPrincipal(1L, "jean");
        UserPrincipal p3 = new UserPrincipal(2L, "paul");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    @Test
    @DisplayName("RolePrincipal - getName retourne le rôle")
    void rolePrincipal_getName() {
        RolePrincipal role = new RolePrincipal("ROLE_USER");
        assertEquals("ROLE_USER", role.getName());
        assertEquals("ROLE_USER", role.getRole());
    }

    @Test
    @DisplayName("RolePrincipal - equals et hashCode")
    void rolePrincipal_equality() {
        RolePrincipal r1 = new RolePrincipal("ROLE_USER");
        RolePrincipal r2 = new RolePrincipal("ROLE_USER");
        RolePrincipal r3 = new RolePrincipal("ROLE_ADMIN");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }

    // ===== Tests TokenCallback =====

    @Test
    @DisplayName("TokenCallback - getter/setter fonctionne")
    void tokenCallback_getSet() {
        TokenCallback cb = new TokenCallback();
        assertNull(cb.getToken());
        cb.setToken("abc123");
        assertEquals("abc123", cb.getToken());
    }
}
