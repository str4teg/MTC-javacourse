package com.mipt.andreysofronov.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads {@code Authorization: Bearer <token>}, validates signature and expiration, and populates
 * the {@link SecurityContextHolder}. Intentionally <b>not</b> a Spring-managed component — it is
 * instantiated inside {@code SecurityConfig} and added to the chain, so it never leaks into
 * {@code @WebMvcTest} slices and is not double-registered as a servlet filter.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null
        && header.startsWith(BEARER_PREFIX)
        && SecurityContextHolder.getContext().getAuthentication() == null) {

      String token = header.substring(BEARER_PREFIX.length()).trim();
      try {
        Jws<Claims> jws = jwtService.parse(token);
        Claims claims = jws.getPayload();
        String username = claims.getSubject();
        List<SimpleGrantedAuthority> authorities =
            jwtService.extractAuthorities(claims).stream().map(SimpleGrantedAuthority::new).toList();

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(username, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Authenticated user={} authorities={} token={}",
            username, authorities, TokenMasker.mask(token));
      } catch (JwtException ex) {
        // Invalid/expired token: leave the context empty so the entry point answers 401 only if
        // the requested resource actually requires authentication. Never log the raw token.
        SecurityContextHolder.clearContext();
        log.warn("Rejected JWT ({}) token={}", ex.getMessage(), TokenMasker.mask(token));
      }
    }

    filterChain.doFilter(request, response);
  }
}
