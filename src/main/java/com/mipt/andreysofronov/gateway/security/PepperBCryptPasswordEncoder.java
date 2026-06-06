package com.mipt.andreysofronov.gateway.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt encoder with a server-side pepper (loaded from properties). The pepper is concatenated
 * with the raw password before hashing, so a leaked password hash cannot be brute-forced without
 * also knowing the application-level pepper. Passwords are never stored in clear text — only the
 * BCrypt digest of {@code pepper + rawPassword} is kept.
 */
public class PepperBCryptPasswordEncoder implements PasswordEncoder {

  private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();
  private final String pepper;

  public PepperBCryptPasswordEncoder(String pepper) {
    this.pepper = pepper == null ? "" : pepper;
  }

  @Override
  public String encode(CharSequence rawPassword) {
    return delegate.encode(pepper + rawPassword);
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    return delegate.matches(pepper + rawPassword, encodedPassword);
  }
}
