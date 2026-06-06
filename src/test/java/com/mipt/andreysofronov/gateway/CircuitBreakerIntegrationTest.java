package com.mipt.andreysofronov.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.mipt.andreysofronov.gateway.service.TasksGatewayService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Scenarios 12/13: repeatedly hitting a failing upstream (the emulator's {@code mode=500}) trips
 * the {@code @CircuitBreaker("externalApi")} to OPEN; once OPEN, calls fast-fail and are served by
 * the gracefully-degrading fallback instead of hammering the broken dependency.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
      "server.port=18082",
      "external.api.base-url=http://localhost:18082/external/v1",
      // never let the rate limiter interfere here
      "resilience4j.ratelimiter.instances.externalApi.limit-for-period=10000",
      "resilience4j.circuitbreaker.instances.externalApi.sliding-window-type=COUNT_BASED",
      "resilience4j.circuitbreaker.instances.externalApi.sliding-window-size=5",
      "resilience4j.circuitbreaker.instances.externalApi.minimum-number-of-calls=3",
      "resilience4j.circuitbreaker.instances.externalApi.failure-rate-threshold=50",
      "resilience4j.circuitbreaker.instances.externalApi.wait-duration-in-open-state=60s",
      "resilience4j.circuitbreaker.instances.externalApi.automatic-transition-from-open-to-half-open-enabled=false"
    })
@ActiveProfiles("test")
class CircuitBreakerIntegrationTest {

  @Autowired private TasksGatewayService gateway;
  @Autowired private CircuitBreakerRegistry circuitBreakerRegistry;

  @BeforeEach
  void resetBreaker() {
    circuitBreakerRegistry.circuitBreaker("externalApi").reset();
  }

  @Test
  void breakerOpensAfterRepeatedFailuresAndFallsBackGracefully() {
    CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("externalApi");
    assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

    List<String> results = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      // Each failing call degrades gracefully (no exception escapes) thanks to the fallback.
      results.add(gateway.probeUnstable("500"));
    }

    assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    assertThat(results).allMatch(r -> r.startsWith("degraded"));

    // A further call while OPEN short-circuits immediately and still returns the fallback value.
    assertThat(gateway.probeUnstable("500")).startsWith("degraded");
  }
}
