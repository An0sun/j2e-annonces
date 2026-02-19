package org.j2e.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;

/**
 * LoginModule JAAS pour la validation de token Bearer.
 *
 * Reçoit un token via TokenCallback, le valide via TokenStore,
 * et reconstitue l'identité utilisateur dans le Subject.
 *
 * Domaine JAAS : MasterAnnonceToken
 */
public class TokenLoginModule implements LoginModule {

    private static final Logger log = LoggerFactory.getLogger(TokenLoginModule.class);

    private Subject subject;
    private CallbackHandler callbackHandler;
    private boolean loginSucceeded = false;
    private boolean commitSucceeded = false;

    private UserPrincipal userPrincipal;
    private RolePrincipal rolePrincipal;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        // Récupérer le token via le callback custom
        TokenCallback tokenCallback = new TokenCallback();

        try {
            callbackHandler.handle(new Callback[] { tokenCallback });
        } catch (IOException | UnsupportedCallbackException e) {
            log.error("Erreur lors de la récupération du token JAAS", e);
            throw new LoginException("Impossible de récupérer le token: " + e.getMessage());
        }

        String token = tokenCallback.getToken();
        if (token == null || token.isEmpty()) {
            throw new LoginException("Token absent");
        }

        // Valider le token via TokenStore
        log.debug("JAAS TokenLoginModule: validation du token");
        TokenStore.TokenInfo tokenInfo = TokenStore.getInstance().validateToken(token);

        if (tokenInfo == null) {
            log.warn("JAAS TokenLoginModule: token invalide ou expiré");
            throw new LoginException("Token invalide ou expiré");
        }

        // Token valide - préparer les Principals
        userPrincipal = new UserPrincipal(tokenInfo.getUserId(), tokenInfo.getUsername());
        rolePrincipal = new RolePrincipal(RolePrincipal.ROLE_USER);
        loginSucceeded = true;

        log.info("JAAS TokenLoginModule: token valide pour '{}' (id={})",
                tokenInfo.getUsername(), tokenInfo.getUserId());
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!loginSucceeded) {
            return false;
        }

        subject.getPrincipals().add(userPrincipal);
        subject.getPrincipals().add(rolePrincipal);
        commitSucceeded = true;

        log.debug("JAAS TokenLoginModule: commit - Principals ajoutés au Subject");
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        if (!loginSucceeded) {
            return false;
        }
        if (commitSucceeded) {
            logout();
        }
        loginSucceeded = false;
        userPrincipal = null;
        rolePrincipal = null;
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        subject.getPrincipals().remove(rolePrincipal);
        loginSucceeded = false;
        commitSucceeded = false;
        userPrincipal = null;
        rolePrincipal = null;
        log.debug("JAAS TokenLoginModule: logout effectué");
        return true;
    }
}
