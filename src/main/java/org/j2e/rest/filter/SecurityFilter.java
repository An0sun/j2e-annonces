package org.j2e.rest.filter;

import org.j2e.rest.dto.ApiErrorResponse;
import org.j2e.security.TokenCallback;
import org.j2e.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Set;

/**
 * Filtre JAX-RS de sécurité stateless avec JAAS.
 *
 * Pour chaque requête protégée (@Secured) :
 * 1. Lit le header Authorization: Bearer <token>
 * 2. Crée un LoginContext("MasterAnnonceToken", callbackHandler)
 * 3. Le TokenLoginModule valide le token et peuple le Subject
 * 4. Extrait UserPrincipal du Subject pour le SecurityContext JAX-RS
 *
 * Exercice 5 Bonus (JAAS) + Exercice 6 (Filtre de sécurité)
 */
@Provider
@Secured
public class SecurityFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER);

        // Vérifier la présence du header
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Requête sans token d'authentification: {} {}",
                    requestContext.getMethod(), requestContext.getUriInfo().getPath());
            abortUnauthorized(requestContext, "Token d'authentification requis");
            return;
        }

        // Extraire le token
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        try {
            // Authentification JAAS via TokenLoginModule
            CallbackHandler callbackHandler = callbacks -> {
                for (Callback callback : callbacks) {
                    if (callback instanceof TokenCallback) {
                        ((TokenCallback) callback).setToken(token);
                    } else {
                        throw new UnsupportedCallbackException(callback);
                    }
                }
            };

            LoginContext loginContext = new LoginContext("MasterAnnonceToken", callbackHandler);
            loginContext.login();

            // Récupérer l'identité depuis le Subject JAAS
            Subject subject = loginContext.getSubject();
            Set<UserPrincipal> principals = subject.getPrincipals(UserPrincipal.class);

            if (principals.isEmpty()) {
                throw new LoginException("Aucun UserPrincipal dans le Subject");
            }

            UserPrincipal userPrincipal = principals.iterator().next();
            log.debug("JAAS authentification réussie pour: {} (id={})",
                    userPrincipal.getUsername(), userPrincipal.getUserId());

            // Injecter l'identité dans le SecurityContext JAX-RS
            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return userPrincipal;
                }

                @Override
                public boolean isUserInRole(String role) {
                    return subject.getPrincipals().stream()
                            .anyMatch(p -> p.getName().equals(role));
                }

                @Override
                public boolean isSecure() {
                    return requestContext.getSecurityContext().isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return "Bearer";
                }
            });

            // Stocker userId et username dans les propriétés de la requête
            requestContext.setProperty("userId", userPrincipal.getUserId());
            requestContext.setProperty("username", userPrincipal.getUsername());

        } catch (LoginException e) {
            log.warn("JAAS token validation échouée: {}", e.getMessage());
            abortUnauthorized(requestContext, "Token invalide ou expiré");
        }
    }

    private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
        ApiErrorResponse error = new ApiErrorResponse(401, "Unauthorized", message);
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(error)
                        .type(MediaType.APPLICATION_JSON)
                        .build());
    }
}
