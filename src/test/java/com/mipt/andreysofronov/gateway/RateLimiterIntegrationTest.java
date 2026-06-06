package com.mipt.andreysofronov.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.mipt.andreysofronov.gateway.dto.CreateTaskRequest;
import com.mipt.andreysofronov.gateway.dto.TaskDto;
import com.mipt.andreysofronov.gateway.service.TasksGatewayService;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Scenario 11: hammering a gateway method past the configured rate limit makes the
 * {@code @RateLimiter("externalApi")} reject calls with {@link RequestNotPermitted}, which the
 * gateway surfaces (mapped to HTTP 429 by the advice). The limiter is shrunk to 3 permits per
 * minute with a 0s timeout for a deterministic assertion.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
      "server.port=18081",
      "external.api.base-url=http://localhost:18081/external/v1",
      "resilience4j.ratelimiter.instances.externalApi.limit-for-period=3",
      "resilience4j.ratelimiter.instances.externalApi.limit-refresh-period=1m",
      "resilience4j.ratelimiter.instances.externalApi.timeout-duration=0s",
      // keep the breaker out of the way for this test
      "resilience4j.circuitbreaker.instances.externalApi.minimum-number-of-calls=1000"
    })
@ActiveProfiles("test")
class RateLimiterIntegrationTest {

  @Autowired private TasksGatewayService gateway;
  @Autowired private TestRestTemplate rest;

  @Test
  void rateLimiterRejectsCallsBeyondLimit() {
    // Seed a task straight into the emulator (bypassing the rate-limited gateway).
    ResponseEntity<TaskDto> seeded =
        rest.postForEntity(
            "http://localhost:18081/external/v1/tasks",
            new CreateTaskRequest("seed", false),
            TaskDto.class);
    Long id = seeded.getBody().id();

    int permitted = 0;
    int throttled = 0;
    for (int i = 0; i < 15; i++) {
      try {
        gateway.getTask(id);
        permitted++;
      } catch (RequestNotPermitted ex) {
        throttled++;
      }
    }

    // At most 3 calls go through per refresh window; the rest are rejected by the rate limiter.
    assertThat(permitted).isBetween(1, 3);
    assertThat(throttled).isGreaterThan(0);
    assertThat(permitted + throttled).isEqualTo(15);
  }
}
