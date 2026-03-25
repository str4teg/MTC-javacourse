package com.mipt.andreysofronov.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.profiles.active=test",
      "spring.main.lazy-initialization=false",
      "app.name=test-app",
      "app.version=0-test",
      "app.api.version=2.0.0",
      "openapi.title=T",
      "openapi.description=D",
      "openapi.contact.name=N",
      "openapi.contact.email=n@e.com"
    })
class ScopeDemoControllerIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void requestScope_returnsRequestScopedFields() {
    ResponseEntity<String> r = restTemplate.getForEntity("/api/scope/request", String.class);

    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(r.getBody()).contains("requestId").contains("processingStartedAt");
  }

  @Test
  void prototypeScope_returnsTwoDifferentInstances() {
    ResponseEntity<String> r = restTemplate.getForEntity("/api/scope/prototype", String.class);

    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(r.getBody())
        .contains("firstGeneratorInstanceId")
        .contains("secondGeneratorInstanceId")
        .contains("sameBeanInstance");
  }
}
