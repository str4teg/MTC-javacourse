package com.mipt.andreysofronov.gateway.security;

/**
 * Masks a JWT for logging: keeps only the first and last 6 characters. Full tokens (and any other
 * secrets) must never reach the logs — see lecture on observability / safe logging.
 */
public final class TokenMasker {

  private static final int KEEP = 6;

  private TokenMasker() {}

  public static String mask(String token) {
    if (token == null || token.isBlank()) {
      return "<none>";
    }
    if (token.length() <= KEEP * 2) {
      return "******";
    }
    return token.substring(0, KEEP) + "..." + token.substring(token.length() - KEEP);
  }
}
