package org.j2e.rest.exception;

import org.j2e.rest.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Filet de sécurité : intercepte toutes les exceptions non gérées → 500.
 * 💥 Sans ce mapper, une exception non interceptée rendrait l'API inutilisable.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        log.error("Erreur interne non gérée", exception);

        ApiErrorResponse error = new ApiErrorResponse(
                500, "Internal Server Error",
                "Une erreur interne est survenue. Veuillez réessayer.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
