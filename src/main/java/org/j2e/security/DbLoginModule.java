package org.j2e.security;

import org.j2e.bean.User;
import org.j2e.dao.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;

/**
 * LoginModule JAAS pour l'authentification username/password.
 *
 * Vérifie les credentials via UserRepository et peuple le Subject
 * avec UserPrincipal et RolePrincipal en cas de succès.
 *
 * Domaine JAAS : MasterAnnonceLogin
 */
public class DbLoginModule implements LoginModule {

    private static final Logger log = LoggerFactory.getLogger(DbLoginModule.class);

    private Subject subject;
    private CallbackHandler callbackHandler;
    private boolean loginSucceeded = false;
    private boolean commitSucceeded = false;

    private UserPrincipal userPrincipal;
    private RolePrincipal rolePrincipal;

    // Accessible pour injection dans les tests
    private UserRepository userRepository;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;

        // Permettre l'injection d'un repository custom (pour les tests)
        Object repo = options.get("userRepository");
        if (repo instanceof UserRepository) {
            this.userRepository = (UserRepository) repo;
        } else {
            this.userRepository = new UserRepository();
        }
    }

    @Override
    public boolean login() throws LoginException {
        // Récupérer les credentials via callbacks
        NameCallback nameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);

        try {
            callbackHandler.handle(new Callback[] { nameCallback, passwordCallback });
        } catch (IOException | UnsupportedCallbackException e) {
            log.error("Erreur lors de la récupération des credentials JAAS", e);
            throw new LoginException("Impossible de récupérer les credentials: " + e.getMessage());
        }

        String username = nameCallback.getName();
        char[] passwordChars = passwordCallback.getPassword();
        if (username == null || passwordChars == null) {
            throw new LoginException("Username ou password manquant");
        }
        String password = new String(passwordChars);
        passwordCallback.clearPassword();

        // Vérifier en base via UserRepository
        log.debug("JAAS DbLoginModule: tentative login pour '{}'", username);
        User user = userRepository.findByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            log.warn("JAAS DbLoginModule: échec login pour '{}'", username);
            throw new LoginException("Identifiants incorrects");
        }

        // Login réussi - préparer les Principals
        userPrincipal = new UserPrincipal(user.getId(), user.getUsername());
        rolePrincipal = new RolePrincipal(RolePrincipal.ROLE_USER);
        loginSucceeded = true;

        log.info("JAAS DbLoginModule: login réussi pour '{}' (id={})", username, user.getId());
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!loginSucceeded) {
            return false;
        }

        // Ajouter les Principals au Subject
        subject.getPrincipals().add(userPrincipal);
        subject.getPrincipals().add(rolePrincipal);
        commitSucceeded = true;

        log.debug("JAAS DbLoginModule: commit - Principals ajoutés au Subject");
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
        log.debug("JAAS DbLoginModule: logout effectué");
        return true;
    }
}
