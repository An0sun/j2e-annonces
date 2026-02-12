package org.j2e.rest.filter;

import org.j2e.rest.dto.ApiErrorResponse;
import org.j2e.security.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

/**
 * Filtre JAX-RS de sécurité stateless.
 *
 * Lit le header Authorization: Bearer <token>, vérifie le token via TokenStore,
 * et injecte l'identité dans le SecurityContext JAX-RS.
 *
 * Appliqué uniquement aux endpoints annotés avec @Secured.
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

        // Extraire et valider le token
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        TokenStore.TokenInfo tokenInfo = TokenStore.getInstance().validateToken(token);

        if (tokenInfo == null) {
            log.warn("Token invalide ou expiré");
            abortUnauthorized(requestContext, "Token invalide ou expiré");
            return;
        }

        // Injecter l'identité dans le SecurityContext
        log.debug("Authentification réussie pour l'utilisateur: {} (id={})",
                tokenInfo.getUsername(), tokenInfo.getUserId());

        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> tokenInfo.getUserId().toString();
            }

            @Override
            public boolean isUserInRole(String role) {
                return true; // Pas de gestion de rôles pour l'instant
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

        // Stocker l'userId et le username dans les propriétés de la requête
        requestContext.setProperty("userId", tokenInfo.getUserId());
        requestContext.setProperty("username", tokenInfo.getUsername());
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
