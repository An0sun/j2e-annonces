package org.j2e.rest.resource;

import org.j2e.rest.dto.ApiErrorResponse;
import org.j2e.rest.dto.LoginDTO;
import org.j2e.rest.dto.TokenResponseDTO;
import org.j2e.security.TokenStore;
import org.j2e.security.UserPrincipal;
import org.j2e.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * Resource REST pour l'authentification.
 * Exercice 5 : Login stateless avec génération de token.
 * Exercice 5 Bonus : Authentification via JAAS (LoginContext + DbLoginModule).
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger log = LoggerFactory.getLogger(AuthResource.class);
    private final UserService userService = new UserService();

    /**
     * POST /api/login - Authentification via JAAS.
     *
     * 1. Crée un LoginContext("MasterAnnonceLogin", callbackHandler)
     * 2. loginContext.login() → DbLoginModule vérifie en base
     * 3. Récupère le Subject authentifié avec UserPrincipal
     * 4. Génère un token UUID
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginDTO dto) {
        log.info("POST /api/login - username={}", dto.getUsername());

        try {
            // CallbackHandler pour fournir les credentials au DbLoginModule
            CallbackHandler callbackHandler = callbacks -> {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(dto.getUsername());
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(dto.getPassword().toCharArray());
                    } else {
                        throw new UnsupportedCallbackException(callback);
                    }
                }
            };

            // Authentification JAAS
            LoginContext loginContext = new LoginContext("MasterAnnonceLogin", callbackHandler);
            loginContext.login();

            // Récupérer l'identité depuis le Subject
            Subject subject = loginContext.getSubject();
            Set<UserPrincipal> principals = subject.getPrincipals(UserPrincipal.class);
            if (principals.isEmpty()) {
                throw new LoginException("Aucun UserPrincipal dans le Subject");
            }

            UserPrincipal userPrincipal = principals.iterator().next();
            log.info("JAAS login réussi pour: {} (id={})", userPrincipal.getUsername(), userPrincipal.getUserId());

            // Générer un token
            String token = TokenStore.getInstance().generateToken(
                    userPrincipal.getUserId(), userPrincipal.getUsername());

            TokenResponseDTO response = new TokenResponseDTO(
                    token, userPrincipal.getUsername(), userPrincipal.getUserId());
            return Response.ok(response).build();

        } catch (LoginException e) {
            log.warn("Échec JAAS login pour '{}': {}", dto.getUsername(), e.getMessage());
            ApiErrorResponse error = new ApiErrorResponse(
                    401, "Unauthorized", "Identifiants incorrects");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error)
                    .build();
        }
    }

    /**
     * POST /api/register - Inscription.
     */
    @POST
    @Path("/register")
    public Response register(@Valid org.j2e.rest.dto.RegisterDTO dto) {
        log.info("POST /api/register - username={}", dto.getUsername());

        try {
            org.j2e.bean.User user = new org.j2e.bean.User(dto.getUsername(), dto.getEmail(), dto.getPassword());
            userService.register(user);

            log.info("Inscription réussie pour: {}", dto.getUsername());
            return Response.status(Response.Status.CREATED)
                    .entity(java.util.Map.of(
                            "message", "Inscription réussie",
                            "username", user.getUsername()))
                    .build();
        } catch (IllegalArgumentException e) {
            ApiErrorResponse error = new ApiErrorResponse(
                    409, "Conflict", e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                    .entity(error)
                    .build();
        }
    }
}
