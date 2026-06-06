package com.mipt.andreysofronov.gateway.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * One structured access-log line per request:
 *
 * <pre>HTTP {method} {path} -&gt; status={status} timeMs={ms} trace={traceId}</pre>
 *
 * Runs just inside {@link TraceIdFilter} but still ahead of Spring Security, so the logged status
 * reflects security outcomes (401/403) too. Only method, path, status, duration and traceId are
 * logged — never headers, bodies or tokens.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AccessLogFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger("access-log");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    long start = System.nanoTime();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long ms = (System.nanoTime() - start) / 1_000_000;
      log.info(
          "HTTP {} {} -> status={} timeMs={} trace={}",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          ms,
          MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY));
    }
  }
}
