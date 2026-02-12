package org.j2e.rest.exception;

import org.j2e.rest.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Intercepte les BusinessException → 409 Conflict.
 */
@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    private static final Logger log = LoggerFactory.getLogger(BusinessExceptionMapper.class);

    @Override
    public Response toResponse(BusinessException exception) {
        log.warn("Business conflict: {}", exception.getMessage());

        ApiErrorResponse error = new ApiErrorResponse(
                409, "Conflict", exception.getMessage());

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
