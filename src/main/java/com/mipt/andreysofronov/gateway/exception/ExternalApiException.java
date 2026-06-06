package com.mipt.andreysofronov.gateway.exception;

/**
 * Raised when the external tasks service answers with a 5xx status, an unexpected content-type
 * (e.g. HTML instead of JSON), or is unreachable / times out. This is the failure that the
 * circuit breaker is configured to count (see {@code resilience4j.circuitbreaker} in
 * application.yaml).
 */
public class ExternalApiException extends RuntimeException {

  private final int upstreamStatus;

  public ExternalApiException(int upstreamStatus, String message) {
    super(message);
    this.upstreamStatus = upstreamStatus;
  }

  public ExternalApiException(int upstreamStatus, String message, Throwable cause) {
    super(message, cause);
    this.upstreamStatus = upstreamStatus;
  }

  /** Upstream HTTP status that triggered this exception, or 0 for transport-level failures. */
  public int getUpstreamStatus() {
    return upstreamStatus;
  }
}
