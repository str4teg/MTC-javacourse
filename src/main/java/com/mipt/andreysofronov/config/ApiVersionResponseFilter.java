package com.mipt.andreysofronov.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Integer.MIN_VALUE)
public class ApiVersionResponseFilter extends OncePerRequestFilter {

  public static final String HEADER_API_VERSION = "X-API-Version";

  private final String apiVersion;

  public ApiVersionResponseFilter(@Value("${app.api.version}") String apiVersion) {
    this.apiVersion = apiVersion;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    response.setHeader(HEADER_API_VERSION, apiVersion);
    filterChain.doFilter(request, response);
  }
}
