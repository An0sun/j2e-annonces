package com.masterannonce.adapter.rest.exception;

import com.masterannonce.application.dto.ApiErrorResponse;
import com.masterannonce.domain.exception.BusinessException;
import com.masterannonce.domain.exception.ResourceNotFoundException;
import com.masterannonce.domain.exception.UnauthorizedActionException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler — verifies HTTP status and response body for each exception type.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("ResourceNotFoundException → 404")
    void handleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Annonce", 42L);

        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Not Found");
    }

    @Test
    @DisplayName("BusinessException → 400")
    void handleBusiness() {
        BusinessException ex = new BusinessException("Identifiants invalides");

        ResponseEntity<ApiErrorResponse> response = handler.handleBusiness(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("Identifiants invalides");
    }

    @Test
    @DisplayName("UnauthorizedActionException → 403")
    void handleUnauthorized() {
        UnauthorizedActionException ex = new UnauthorizedActionException("Pas autorisé");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnauthorized(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody().error()).isEqualTo("Forbidden");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException → 400 with field errors")
    void handleValidation() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "dto");
        bindingResult.addError(new FieldError("dto", "title", "ne peut pas être vide"));
        bindingResult.addError(new FieldError("dto", "email", "doit être valide"));

        MethodParameter param = new MethodParameter(
            this.getClass().getDeclaredMethod("handleValidation"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Error");
        assertThat(response.getBody().details()).hasSize(2);
    }

    @Test
    @DisplayName("OptimisticLockException → 409")
    void handleOptimisticLock() {
        OptimisticLockException ex = new OptimisticLockException("concurrent modification");

        ResponseEntity<ApiErrorResponse> response = handler.handleOptimisticLock(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
    }

    @Test
    @DisplayName("IllegalStateException → 400")
    void handleIllegalState() {
        IllegalStateException ex = new IllegalStateException("Cannot transition");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalState(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("Generic Exception → 500")
    void handleGeneral() {
        Exception ex = new RuntimeException("unexpected");

        ResponseEntity<ApiErrorResponse> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
    }
}
