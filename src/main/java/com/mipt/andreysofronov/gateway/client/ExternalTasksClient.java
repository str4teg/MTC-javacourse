package com.mipt.andreysofronov.gateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.andreysofronov.gateway.dto.CreateTaskRequest;
import com.mipt.andreysofronov.gateway.dto.TaskDto;
import com.mipt.andreysofronov.gateway.exception.ExternalApiException;
import com.mipt.andreysofronov.gateway.exception.ExternalTaskNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Thin HTTP client over the external tasks service (lecture 10). Demonstrates the required HTTP
 * semantics:
 *
 * <ul>
 *   <li>explicit {@code Content-Type: application/json} on writes and {@code Accept:
 *       application/json} on reads;
 *   <li>query params assembled with {@code uriBuilder} (never string concatenation);
 *   <li>{@code 201 Created} → reads the {@code Location} header;
 *   <li>{@code 204 No Content} → {@code toBodilessEntity()} (no JSON parsing);
 *   <li>{@code 404} → parses the {@code ProblemDetail} body into {@link ExternalTaskNotFoundException};
 *   <li>other {@code 4xx/5xx} → {@link ExternalApiException};
 *   <li>unexpected content-type (HTML instead of JSON) → logs a length-limited body snippet.
 * </ul>
 *
 * <p>Lists are decoded through {@link ParameterizedTypeReference} to preserve the generic type.
 */
@Component
public class ExternalTasksClient {

  private static final Logger log = LoggerFactory.getLogger(ExternalTasksClient.class);
  private static final int MAX_LOGGED_BODY = 256;

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public ExternalTasksClient(RestClient externalApiRestClient, ObjectMapper objectMapper) {
    this.restClient = externalApiRestClient;
    this.objectMapper = objectMapper;
  }

  public TaskDto create(CreateTaskRequest request) {
    try {
      ResponseEntity<TaskDto> response =
          restClient
              .post()
              .uri("/tasks")
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .body(request)
              .retrieve()
              .onStatus(HttpStatusCode::isError, this::handleError)
              .toEntity(TaskDto.class);

      URI location = response.getHeaders().getLocation();
      log.info("Created external task id={} location={}",
          response.getBody() != null ? response.getBody().id() : null, location);
      return response.getBody();
    } catch (ResourceAccessException ex) {
      throw transportFailure("POST /tasks", ex);
    }
  }

  public TaskDto get(Long id) {
    try {
      return restClient
          .get()
          .uri("/tasks/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .onStatus(HttpStatusCode::isError, this::handleError)
          .body(TaskDto.class);
    } catch (ResourceAccessException ex) {
      throw transportFailure("GET /tasks/" + id, ex);
    }
  }

  public List<TaskDto> list(Boolean completed, Integer limit) {
    try {
      List<TaskDto> result =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/tasks")
                          .queryParamIfPresent("completed", Optional.ofNullable(completed))
                          .queryParamIfPresent("limit", Optional.ofNullable(limit))
                          .build())
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .onStatus(HttpStatusCode::isError, this::handleError)
              .body(new ParameterizedTypeReference<List<TaskDto>>() {});
      return result != null ? result : List.of();
    } catch (ResourceAccessException ex) {
      throw transportFailure("GET /tasks", ex);
    }
  }

  public void delete(Long id) {
    try {
      // 204 No Content: drain the response without attempting to parse a JSON body.
      restClient
          .delete()
          .uri("/tasks/{id}", id)
          .retrieve()
          .onStatus(HttpStatusCode::isError, this::handleError)
          .toBodilessEntity();
      log.info("Deleted external task id={}", id);
    } catch (ResourceAccessException ex) {
      throw transportFailure("DELETE /tasks/" + id, ex);
    }
  }

  /** Hits the emulator's unstable endpoint; used to demonstrate circuit-breaker behaviour. */
  public String probeUnstable(String mode) {
    try {
      return restClient
          .get()
          .uri(uriBuilder -> uriBuilder.path("/unstable").queryParam("mode", mode).build())
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .onStatus(HttpStatusCode::isError, this::handleError)
          .body(String.class);
    } catch (ResourceAccessException ex) {
      throw transportFailure("GET /unstable?mode=" + mode, ex);
    }
  }

  // ---------------------------------------------------------------------------------------------

  private void handleError(HttpRequest request, ClientHttpResponse response) throws IOException {
    HttpStatusCode status = response.getStatusCode();
    MediaType contentType = response.getHeaders().getContentType();
    byte[] bodyBytes = response.getBody().readAllBytes();

    boolean jsonLike =
        contentType != null
            && (contentType.isCompatibleWith(MediaType.APPLICATION_JSON)
                || contentType.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

    if (!jsonLike) {
      // Unexpected content-type (e.g. an HTML 502 page from a proxy): log a bounded snippet only.
      log.warn(
          "External API returned status={} with unexpected content-type={}; body snippet=[{}]",
          status.value(),
          contentType,
          limit(new String(bodyBytes, StandardCharsets.UTF_8)));
      throw new ExternalApiException(
          status.value(), "Unexpected content-type from external API: " + contentType);
    }

    String detail = extractProblemDetail(bodyBytes);
    if (status.value() == 404) {
      Long id = extractTaskId(bodyBytes);
      throw new ExternalTaskNotFoundException(id, detail);
    }
    log.warn("External API error status={} detail={}", status.value(), detail);
    throw new ExternalApiException(
        status.value(), "External API error " + status.value() + ": " + detail);
  }

  private String extractProblemDetail(byte[] bodyBytes) {
    try {
      JsonNode node = objectMapper.readTree(bodyBytes);
      JsonNode detail = node.path("detail");
      if (!detail.isMissingNode() && !detail.isNull()) {
        return detail.asText();
      }
    } catch (IOException ignored) {
      // fall through to a bounded raw snippet
    }
    return limit(new String(bodyBytes, StandardCharsets.UTF_8));
  }

  private Long extractTaskId(byte[] bodyBytes) {
    try {
      JsonNode node = objectMapper.readTree(bodyBytes);
      JsonNode taskId = node.path("taskId");
      if (taskId.isIntegralNumber()) {
        return taskId.asLong();
      }
    } catch (IOException ignored) {
      // no id available
    }
    return null;
  }

  private ExternalApiException transportFailure(String operation, ResourceAccessException ex) {
    log.warn("External API transport failure on {}: {}", operation, ex.getMessage());
    // upstreamStatus 0 = no HTTP response (timeout / connection error); still counted by the breaker.
    return new ExternalApiException(0, "External API unreachable or timed out on " + operation, ex);
  }

  private static String limit(String body) {
    if (body == null) {
      return "";
    }
    String trimmed = body.strip();
    return trimmed.length() <= MAX_LOGGED_BODY
        ? trimmed
        : trimmed.substring(0, MAX_LOGGED_BODY) + "…(truncated)";
  }
}
