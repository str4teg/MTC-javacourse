package com.mipt.andreysofronov.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * End-to-end gateway flow over real HTTP (scenarios 6–10): the internal {@code /api/v1/tasks}
 * endpoints call the bundled emulator through the {@code RestClient}. Runs on a fixed port so the
 * gateway's configured base URL can point back at this same application.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
      "server.port=18083",
      "external.api.base-url=http://localhost:18083/external/v1",
      "resilience4j.ratelimiter.instances.externalApi.limit-for-period=1000"
    })
@ActiveProfiles("test")
class GatewayE2EIntegrationTest {

  private static final String BASE = "http://localhost:18083";

  @Autowired private TestRestTemplate rest;
  @Autowired private ObjectMapper objectMapper;

  private String token;

  @BeforeEach
  void login() throws Exception {
    HttpHeaders json = new HttpHeaders();
    json.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> response =
        rest.postForEntity(
            BASE + "/api/v1/auth/login",
            new HttpEntity<>("{\"username\":\"user\",\"password\":\"password\"}", json),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    token = objectMapper.readTree(response.getBody()).get("accessToken").asText();
  }

  private HttpHeaders authHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private HttpHeaders authJsonHeaders() {
    HttpHeaders headers = authHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  @Test
  void create_read_list_delete_roundTrip() throws Exception {
    // POST -> external returns 201+Location, gateway echoes the created task with its own Location.
    ResponseEntity<String> created =
        rest.exchange(
            BASE + "/api/v1/tasks",
            HttpMethod.POST,
            new HttpEntity<>("{\"title\":\"buy milk\",\"completed\":false}", authJsonHeaders()),
            String.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(created.getHeaders().getLocation()).isNotNull();
    long id = objectMapper.readTree(created.getBody()).get("id").asLong();

    // GET single
    ResponseEntity<String> fetched =
        rest.exchange(
            BASE + "/api/v1/tasks/" + id,
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            String.class);
    assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(objectMapper.readTree(fetched.getBody()).get("title").asText()).isEqualTo("buy milk");

    // GET list with query params completed=false&limit=10
    ResponseEntity<String> list =
        rest.exchange(
            BASE + "/api/v1/tasks?completed=false&limit=10",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            String.class);
    assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode array = objectMapper.readTree(list.getBody());
    assertThat(array.isArray()).isTrue();
    assertThat(array.size()).isGreaterThanOrEqualTo(1);

    // DELETE -> external 204 -> gateway 204 (no JSON parsing)
    ResponseEntity<Void> deleted =
        rest.exchange(
            BASE + "/api/v1/tasks/" + id,
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders()),
            Void.class);
    assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    // GET deleted -> external 404 ProblemDetail -> gateway 404
    ResponseEntity<String> afterDelete =
        rest.exchange(
            BASE + "/api/v1/tasks/" + id,
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            String.class);
    assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void getMissingTask_returns404WithDetail() throws Exception {
    ResponseEntity<String> response =
        rest.exchange(
            BASE + "/api/v1/tasks/999999",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(objectMapper.readTree(response.getBody()).get("message").asText())
        .contains("999999");
  }

  @Test
  void tasksEndpoint_withoutToken_returns401() {
    ResponseEntity<String> response =
        rest.getForEntity(BASE + "/api/v1/tasks/1", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void traceId_isEchoedInResponseHeader() {
    HttpHeaders headers = authHeaders();
    headers.set("X-Trace-Id", "e2e-trace-77");
    ResponseEntity<String> response =
        rest.exchange(
            BASE + "/api/v1/tasks?limit=5",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);
    assertThat(response.getHeaders().getFirst("X-Trace-Id")).isEqualTo("e2e-trace-77");
  }
}
