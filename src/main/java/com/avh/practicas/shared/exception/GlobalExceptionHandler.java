package com.avh.practicas.shared.exception;

import com.avh.practicas.cierre.exception.CierreNoPermitidoException;
import com.avh.practicas.shared.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo centralizado de excepciones HTTP para toda la API.
 * Evita duplicar lógica de errores en cada controlador (SRP / DRY).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class, NegocioException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequest(RuntimeException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(CierreNoPermitidoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleCierreNoPermitido(CierreNoPermitidoException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ApiResponse.error("Error de validación en los datos enviados", errores);
    }

    @ExceptionHandler({ForbiddenAccessException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleForbidden(RuntimeException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleIllegalState(IllegalStateException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException ex) {
        return ApiResponse.error("No se pudo completar la operación por conflicto de datos. Verifique correos duplicados u otros valores únicos.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleNotReadable(HttpMessageNotReadableException ex) {
        return ApiResponse.error("El cuerpo de la solicitud no es válido: " + ex.getMostSpecificCause().getMessage());
    }
}
