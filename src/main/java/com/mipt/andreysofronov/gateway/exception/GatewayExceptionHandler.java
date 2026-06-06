package com.mipt.andreysofronov.gateway.exception;

import com.mipt.andreysofronov.dto.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates gateway / resilience / auth exceptions into JSON {@link ErrorResponse} bodies.
 *
 * <p>Ordered ahead of the legacy {@code GlobalExceptionHandler} so these specific types are handled
 * here; everything it does not declare falls through to the legacy advice.
 *
 * <ul>
 *   <li>{@link ExternalTaskNotFoundException} → 404 (detail taken from upstream ProblemDetail);
 *   <li>{@link ExternalApiException} (5xx / bad content-type / timeout) → 502 Bad Gateway;
 *   <li>{@link RequestNotPermitted} (rate limiter) → 429 with {@code Retry-After};
 *   <li>{@link CallNotPermittedException} (circuit OPEN) → 503;
 *   <li>{@link AuthenticationException} (bad login credentials) → 401.
 * </ul>
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class);

  @ExceptionHandler(ExternalTaskNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      ExternalTaskNotFoundException ex, HttpServletRequest request) {
    ErrorResponse body =
        build(request, HttpStatus.NOT_FOUND, ex.getMessage(), detailMap("taskId", ex.getTaskId()));
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(ExternalApiException.class)
  public ResponseEntity<ErrorResponse> handleExternalApi(
      ExternalApiException ex, HttpServletRequest request) {
    log.warn("Mapping ExternalApiException (upstreamStatus={}) to 502: {}",
        ex.getUpstreamStatus(), ex.getMessage());
    ErrorResponse body =
        build(
            request,
            HttpStatus.BAD_GATEWAY,
            "Upstream tasks service failed: " + ex.getMessage(),
            detailMap("upstreamStatus", ex.getUpstreamStatus()));
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
  }

  @ExceptionHandler(RequestNotPermitted.class)
  public ResponseEntity<ErrorResponse> handleRateLimited(
      RequestNotPermitted ex, HttpServletRequest request) {
    ErrorResponse body =
        build(
            request,
            HttpStatus.TOO_MANY_REQUESTS,
            "Rate limit exceeded, please retry later",
            Map.of());
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header(HttpHeaders.RETRY_AFTER, "1")
        .body(body);
  }

  @ExceptionHandler(CallNotPermittedException.class)
  public ResponseEntity<ErrorResponse> handleCircuitOpen(
      CallNotPermittedException ex, HttpServletRequest request) {
    ErrorResponse body =
        build(
            request,
            HttpStatus.SERVICE_UNAVAILABLE,
            "Upstream tasks service is unavailable (circuit open)",
            Map.of());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .header(HttpHeaders.RETRY_AFTER, "5")
        .body(body);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuth(
      AuthenticationException ex, HttpServletRequest request) {
    ErrorResponse body =
        build(request, HttpStatus.UNAUTHORIZED, ex.getMessage(), Map.of());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }

  private static Map<String, Object> detailMap(String key, Object value) {
    if (value == null) {
      return Map.of();
    }
    Map<String, Object> details = new LinkedHashMap<>();
    details.put(key, value);
    return details;
  }

  private static ErrorResponse build(
      HttpServletRequest request, HttpStatus status, String message, Map<String, Object> details) {
    ErrorResponse er = new ErrorResponse();
    er.setTimestamp(Instant.now());
    er.setStatus(status.value());
    er.setError(status.getReasonPhrase());
    er.setMessage(message);
    er.setPath(request.getRequestURI());
    Map<String, Object> merged = new LinkedHashMap<>(details);
    String traceId = MDC.get("traceId");
    if (traceId != null) {
      merged.put("traceId", traceId);
    }
    er.setDetails(merged);
    return er;
  }
}
