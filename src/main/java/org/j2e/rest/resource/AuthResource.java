package org.j2e.rest.resource;

import org.j2e.bean.User;
import org.j2e.rest.dto.ApiErrorResponse;
import org.j2e.rest.dto.LoginDTO;
import org.j2e.rest.dto.TokenResponseDTO;
import org.j2e.security.TokenStore;
import org.j2e.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource REST pour l'authentification.
 * Exercice 5 : Login stateless avec génération de token.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger log = LoggerFactory.getLogger(AuthResource.class);
    private final UserService userService = new UserService();

    /**
     * POST /api/login - Authentification.
     * Vérifie les credentials et retourne un token.
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginDTO dto) {
        log.info("POST /api/login - username={}", dto.getUsername());

        User user = userService.login(dto.getUsername(), dto.getPassword());
        if (user == null) {
            log.warn("Échec de login pour: {}", dto.getUsername());
            ApiErrorResponse error = new ApiErrorResponse(
                    401, "Unauthorized", "Identifiants incorrects");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error)
                    .build();
        }

        // Générer un token
        String token = TokenStore.getInstance().generateToken(user.getId(), user.getUsername());
        log.info("Login réussi pour: {} (id={})", user.getUsername(), user.getId());

        TokenResponseDTO response = new TokenResponseDTO(token, user.getUsername(), user.getId());
        return Response.ok(response).build();
    }

    /**
     * POST /api/register - Inscription.
     */
    @POST
    @Path("/register")
    public Response register(@Valid org.j2e.rest.dto.RegisterDTO dto) {
        log.info("POST /api/register - username={}", dto.getUsername());

        try {
            User user = new User(dto.getUsername(), dto.getEmail(), dto.getPassword());
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
