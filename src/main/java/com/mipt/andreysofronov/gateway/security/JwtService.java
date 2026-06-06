package com.mipt.andreysofronov.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Issues and validates HS256 JWT access tokens. The signing key and TTL are read from properties.
 * Validation verifies both the signature and the {@code exp} claim ({@link #parse(String)} throws
 * {@link io.jsonwebtoken.JwtException} for tampered or expired tokens).
 */
@Service
public class JwtService {

  private static final String AUTHORITIES_CLAIM = "authorities";

  private final SecretKey key;
  private final long expirationMs;

  public JwtService(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.expiration-ms:3600000}") long expirationMs) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  public String generateToken(String username, Collection<String> authorities) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(username)
        .claim(AUTHORITIES_CLAIM, List.copyOf(authorities))
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(expirationMs)))
        .signWith(key)
        .compact();
  }

  /**
   * Verifies the signature and expiration of the token.
   *
   * @throws io.jsonwebtoken.JwtException if the token is malformed, has a bad signature or is
   *     expired
   */
  public Jws<Claims> parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
  }

  @SuppressWarnings("unchecked")
  public List<String> extractAuthorities(Claims claims) {
    Object raw = claims.get(AUTHORITIES_CLAIM);
    if (raw instanceof Collection<?> col) {
      return col.stream().map(String::valueOf).toList();
    }
    return List.of();
  }

  public long getExpirationSeconds() {
    return expirationMs / 1000;
  }
}
