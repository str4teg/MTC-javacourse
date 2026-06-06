package com.mipt.andreysofronov.gateway.api;

import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Role / authority protected resources. Access is enforced by the URL rules in {@code
 * SecurityConfig}:
 *
 * <ul>
 *   <li>{@code GET /api/v1/profile} → {@code ROLE_USER};
 *   <li>{@code GET /api/v1/docs} → {@code READ_PRIVILEGE}.
 * </ul>
 */
@RestController
@RequestMapping("/api/v1")
public class SecuredController {

  @GetMapping("/profile")
  public Map<String, Object> profile(Authentication authentication) {
    return Map.of(
        "username", authentication.getName(),
        "authorities", authorities(authentication),
        "message", "This profile is visible to ROLE_USER");
  }

  @GetMapping("/docs")
  public Map<String, Object> docs(Authentication authentication) {
    return Map.of(
        "title", "Internal Documentation",
        "content", "Privileged documentation visible only to holders of READ_PRIVILEGE.",
        "reader", authentication.getName());
  }

  private static List<String> authorities(Authentication authentication) {
    return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
  }
}
