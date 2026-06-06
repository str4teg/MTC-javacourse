package com.mipt.andreysofronov.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withCreatedEntity;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.andreysofronov.gateway.client.ExternalTasksClient;
import com.mipt.andreysofronov.gateway.dto.CreateTaskRequest;
import com.mipt.andreysofronov.gateway.dto.TaskDto;
import com.mipt.andreysofronov.gateway.exception.ExternalApiException;
import com.mipt.andreysofronov.gateway.exception.ExternalTaskNotFoundException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Unit tests of {@link ExternalTasksClient} against a {@link MockRestServiceServer} — the canonical
 * way to assert RestClient request shaping and response handling: explicit Accept/Content-Type,
 * uriBuilder query encoding, 201+Location, 204 (no body parsing), 404→ProblemDetail→domain
 * exception, 5xx→ExternalApiException, and HTML→safe-logged ExternalApiException.
 */
class ExternalTasksClientTest {

  private static final String BASE = "http://ext.local/external/v1";

  private MockRestServiceServer server;
  private ExternalTasksClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
    server = MockRestServiceServer.bindTo(builder).build();
    client = new ExternalTasksClient(builder.build(), new ObjectMapper());
  }

  @Test
  void create_sendsJsonAndReads201WithLocation() {
    server
        .expect(requestTo(BASE + "/tasks"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
        .andExpect(header("Accept", containsString(MediaType.APPLICATION_JSON_VALUE)))
        .andExpect(jsonPath("$.title").value("buy milk"))
        .andExpect(jsonPath("$.completed").value(false))
        .andRespond(
            withCreatedEntity(URI.create(BASE + "/tasks/7"))
                .body("{\"id\":7,\"title\":\"buy milk\",\"completed\":false}")
                .contentType(MediaType.APPLICATION_JSON));

    TaskDto created = client.create(new CreateTaskRequest("buy milk", false));

    assertThat(created.id()).isEqualTo(7L);
    assertThat(created.title()).isEqualTo("buy milk");
    server.verify();
  }

  @Test
  void list_encodesQueryParamsThroughUriBuilder() {
    server
        .expect(requestTo(BASE + "/tasks?completed=false&limit=10"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Accept", containsString(MediaType.APPLICATION_JSON_VALUE)))
        .andRespond(
            withSuccess(
                "[{\"id\":1,\"title\":\"a\",\"completed\":false}]", MediaType.APPLICATION_JSON));

    List<TaskDto> tasks = client.list(false, 10);

    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).title()).isEqualTo("a");
    server.verify();
  }

  @Test
  void delete_handles204WithoutBodyParsing() {
    server
        .expect(requestTo(BASE + "/tasks/5"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    client.delete(5L); // must not throw and must not attempt to parse a JSON body

    server.verify();
  }

  @Test
  void get404_parsesProblemDetailIntoNotFoundException() {
    server
        .expect(requestTo(BASE + "/tasks/99"))
        .andRespond(
            withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(
                    "{\"type\":\"about:blank\",\"title\":\"Task Not Found\",\"status\":404,"
                        + "\"detail\":\"Task 99 was not found\",\"taskId\":99}"));

    assertThatThrownBy(() -> client.get(99L))
        .isInstanceOf(ExternalTaskNotFoundException.class)
        .hasMessageContaining("Task 99 was not found");
    server.verify();
  }

  @Test
  void get5xx_isWrappedInExternalApiException() {
    server
        .expect(requestTo(BASE + "/tasks/1"))
        .andRespond(
            withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body("{\"status\":500,\"detail\":\"boom\"}"));

    assertThatThrownBy(() -> client.get(1L))
        .isInstanceOf(ExternalApiException.class)
        .hasMessageContaining("boom");
    server.verify();
  }

  @Test
  void htmlInsteadOfJson_isWrappedInExternalApiException() {
    server
        .expect(requestTo(BASE + "/unstable?mode=html"))
        .andRespond(
            withStatus(HttpStatus.BAD_GATEWAY)
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body>502 Bad Gateway</body></html>"));

    assertThatThrownBy(() -> client.probeUnstable("html"))
        .isInstanceOf(ExternalApiException.class)
        .hasMessageContaining("Unexpected content-type");
    server.verify();
  }
}
