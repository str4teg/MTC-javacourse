package com.mipt.andreysofronov.gateway.api;

import com.mipt.andreysofronov.gateway.dto.LoginRequest;
import com.mipt.andreysofronov.gateway.dto.LoginResponse;
import com.mipt.andreysofronov.gateway.security.JwtService;
import com.mipt.andreysofronov.gateway.security.TokenMasker;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code POST /api/v1/auth/login} — verifies credentials and issues a signed JWT access token. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthController(
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    UserDetails user;
    try {
      user = userDetailsService.loadUserByUsername(request.username());
    } catch (UsernameNotFoundException ex) {
      // Do not reveal whether the username exists.
      throw new BadCredentialsException("Invalid username or password");
    }
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BadCredentialsException("Invalid username or password");
    }

    List<String> authorities =
        user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    String token = jwtService.generateToken(user.getUsername(), authorities);
    log.info("Issued JWT for user={} token={}", user.getUsername(), TokenMasker.mask(token));

    return ResponseEntity.ok(
        new LoginResponse(
            token, "Bearer", jwtService.getExpirationSeconds(), user.getUsername(), authorities));
  }
}
