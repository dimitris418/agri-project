package gr.aueb.cf.agriapp.core;

import gr.aueb.cf.agriapp.core.exceptions.*;
import gr.aueb.cf.agriapp.dto.ResponseMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Κεντρικός χειριστής σφαλμάτων. Κάθε exception που ξεφεύγει από controller
 * περνάει από εδώ και μετατρέπεται σε HTTP status με τυποποιημένο σώμα, ώστε
 * οι controllers να μην περιέχουν try/catch.
 */
@ControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        log.warn("Validation failed. Message={}", e.getMessage());
        BindingResult bindingResult = e.getBindingResult();

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AppObjectNotFoundException.class)
    public ResponseEntity<ResponseMessageDTO> handleNotFound(AppObjectNotFoundException e) {
        log.warn("Entity not found. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AppObjectInvalidArgumentException.class)
    public ResponseEntity<ResponseMessageDTO> handleInvalidArgument(AppObjectInvalidArgumentException e) {
        log.warn("Invalid argument. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AppObjectAlreadyExists.class)
    public ResponseEntity<ResponseMessageDTO> handleAlreadyExists(AppObjectAlreadyExists e) {
        log.warn("Entity already exists. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AppObjectNotAuthorizedException.class)
    public ResponseEntity<ResponseMessageDTO> handleNotAuthorized(AppObjectNotAuthorizedException e,
                                                                  WebRequest request) {
        log.warn("Authorization failed for URI={}. Message={}",
                request.getDescription(false), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AppServerException.class)
    public ResponseEntity<ResponseMessageDTO> handleServerException(AppServerException e) {
        log.error("Server error. Code={}, Message={}", e.getCode(), e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO(e.getCode(), "An internal error occurred"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseMessageDTO> handleAccessDenied(AccessDeniedException e, WebRequest request) {
        log.warn("Access denied for URI={}", request.getDescription(false));
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ResponseMessageDTO("ACCESS_DENIED", "Access is denied"));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ResponseMessageDTO> handleDatabaseErrors(DataAccessException e) {
        log.error("Database error", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO("DATABASE_ERROR", "A database error occurred"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessageDTO> handleUnexpected(Exception e, WebRequest request) {
        log.error("Unexpected error for URI={}", request.getDescription(false), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
