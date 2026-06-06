package com.mipt.andreysofronov.gateway.dto;

import java.util.List;

/** Successful login response carrying the signed JWT access token. */
public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    String username,
    List<String> authorities) {}
