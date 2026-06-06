package com.mipt.andreysofronov.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.andreysofronov.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/** Returns a JSON {@code 401} body (never an HTML login page) for unauthenticated requests. */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    ErrorResponse body = new ErrorResponse();
    body.setTimestamp(Instant.now());
    body.setStatus(HttpStatus.UNAUTHORIZED.value());
    body.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    body.setMessage("Authentication required: missing or invalid bearer token");
    body.setPath(request.getRequestURI());
    String traceId = MDC.get("traceId");
    body.setDetails(traceId != null ? Map.of("traceId", traceId) : Map.of());

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), body);
  }
}
