package com.mipt.andreysofronov.exception;

import com.mipt.andreysofronov.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final Environment environment;

  public GlobalExceptionHandler(Environment environment) {
    this.environment = environment;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, Object> details = new LinkedHashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      details.put(fe.getField(), fe.getDefaultMessage());
    }
    ex.getBindingResult()
        .getGlobalErrors()
        .forEach(
            oe -> details.put("_global." + oe.getObjectName(), oe.getDefaultMessage()));
    ErrorResponse body =
        buildError(
            request,
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Validation failed",
            details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    Map<String, Object> details = new LinkedHashMap<>();
    for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
      details.put(v.getPropertyPath().toString(), v.getMessage());
    }
    ErrorResponse body =
        buildError(
            request,
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Constraint validation failed",
            details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameter(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    Map<String, Object> details =
        Map.of(
            "parameter",
            ex.getParameterName(),
            "type",
            ex.getParameterType() != null ? ex.getParameterType() : "");
    ErrorResponse body =
        buildError(
            request,
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Required request parameter is missing: " + ex.getParameterName(),
            details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    Map<String, Object> details = new LinkedHashMap<>();
    Throwable cause = ex.getMostSpecificCause();
    if (cause != null && cause.getMessage() != null) {
      details.put("cause", cause.getMessage());
    }
    ErrorResponse body =
        buildError(
            request,
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Malformed JSON or unreadable request body",
            details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandler(
      NoHandlerFoundException ex, HttpServletRequest request) {
    ErrorResponse body =
        buildError(
            request,
            HttpStatus.NOT_FOUND,
            "Not Found",
            "No handler for " + ex.getHttpMethod() + " " + ex.getRequestURL(),
            Map.of());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTaskNotFound(
      TaskNotFoundException ex, HttpServletRequest request) {
    Map<String, Object> details = Map.of("taskId", ex.getTaskId());
    ErrorResponse body =
        buildError(
            request,
            HttpStatus.NOT_FOUND,
            "Not Found",
            ex.getMessage(),
            details);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    Map<String, Object> details = new LinkedHashMap<>();
    details.put("parameter", ex.getName());
    if (ex.getValue() != null) {
      details.put("value", ex.getValue().toString());
    }
    if (ex.getRequiredType() != null) {
      details.put("expectedType", ex.getRequiredType().getSimpleName());
    }
    ErrorResponse body =
        buildError(
            request,
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Type mismatch for parameter '" + ex.getName() + "'",
            details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatus(
      ResponseStatusException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    if (status == null) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    String reason = ex.getReason();
    String message = reason != null ? reason : status.getReasonPhrase();
    ErrorResponse body =
        buildError(
            request,
            status,
            status.getReasonPhrase(),
            message,
            Map.of());
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception", ex);
    boolean prod = environment.acceptsProfiles(Profiles.of("prod"));
    String message =
        prod
            ? "An unexpected error occurred"
            : (ex.getMessage() != null ? ex.getMessage() : "Internal server error");
    ErrorResponse body =
        buildError(
            request,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            message,
            Map.of());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private static ErrorResponse buildError(
      HttpServletRequest request,
      HttpStatus status,
      String error,
      String message,
      Map<String, Object> details) {
    ErrorResponse er = new ErrorResponse();
    er.setTimestamp(Instant.now());
    er.setStatus(status.value());
    er.setError(error);
    er.setMessage(message);
    er.setPath(request.getRequestURI());
    er.setDetails(details != null ? details : Map.of());
    return er;
  }

}
