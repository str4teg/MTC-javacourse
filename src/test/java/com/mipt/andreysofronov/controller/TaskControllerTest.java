package com.mipt.andreysofronov.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mipt.andreysofronov.dto.TaskCreateDto;
import com.mipt.andreysofronov.dto.TaskResponseDto;
import com.mipt.andreysofronov.dto.TaskUpdateDto;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.TaskRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.profiles.active=test",
      "app.name=test-app",
      "app.version=0-test",
      "app.api.version=2.0.0"
    })
class TaskControllerTest {

  @Autowired private TestRestTemplate restTemplate;

  @MockitoBean(name = "inMemoryTaskRepository")
  private TaskRepository taskRepository;

  private final Map<Long, Task> store = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong();

  @BeforeEach
  void configureRepositoryMock() {
    reset(taskRepository);
    store.clear();
    idSeq.set(0);

    when(taskRepository.save(any(Task.class)))
        .thenAnswer(
            inv -> {
              Task t = inv.getArgument(0);
              if (t.getId() == null) {
                t.setId(idSeq.incrementAndGet());
              }
              store.put(t.getId(), t);
              return t;
            });

    when(taskRepository.findAll()).thenAnswer(inv -> new ArrayList<>(store.values()));

    when(taskRepository.findById(anyLong()))
        .thenAnswer(
            inv -> {
              Long id = inv.getArgument(0);
              return Optional.ofNullable(store.get(id));
            });

    doAnswer(
            inv -> {
              store.remove(inv.getArgument(0));
              return null;
            })
        .when(taskRepository)
        .deleteById(anyLong());
  }

  private void warmUpTaskService() {
    ResponseEntity<String> r = restTemplate.getForEntity("/api/tasks", String.class);
    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void getAllTasks_returnsOkWithBody() {
    warmUpTaskService();
    restTemplate.postForEntity(
        "/api/tasks", jsonEntity(createDto("abc")), TaskResponseDto.class);

    ResponseEntity<TaskResponseDto[]> response =
        restTemplate.getForEntity("/api/tasks", TaskResponseDto[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).extracting(TaskResponseDto::getTitle).contains("abc");
  }

  @Test
  void getAllTasks_whenFindAllFails_returnsInternalServerError() {
    warmUpTaskService();
    when(taskRepository.findAll()).thenThrow(new RuntimeException("storage unavailable"));

    ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void getTaskById_whenFound_returnsOk() {
    warmUpTaskService();
    TaskResponseDto created =
        restTemplate
            .postForEntity("/api/tasks", jsonEntity(createDto("xxx")), TaskResponseDto.class)
            .getBody();
    assertThat(created).isNotNull();

    ResponseEntity<TaskResponseDto> response =
        restTemplate.getForEntity("/api/tasks/" + created.getId(), TaskResponseDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("xxx");
  }

  @Test
  void getTaskById_whenMissing_returnsNotFound() {
    warmUpTaskService();

    ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/99", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void getTaskById_whenInvalidPath_returnsClientOrServerError() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/api/tasks/not-a-number", String.class);

    assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError())
        .isTrue();
  }

  @Test
  void createTask_returnsCreated() {
    warmUpTaskService();
    TaskCreateDto input = createDto("new");

    ResponseEntity<TaskResponseDto> response =
        restTemplate.postForEntity("/api/tasks", jsonEntity(input), TaskResponseDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("new");
  }

  @Test
  void createTask_whenBodyInvalid_returnsClientOrServerError() {
    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> badJson = new HttpEntity<>("{invalid", headers);

    ResponseEntity<String> response = restTemplate.postForEntity("/api/tasks", badJson, String.class);

    assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError())
        .isTrue();
  }

  @Test
  void createTask_whenSaveFails_returnsInternalServerError() {
    warmUpTaskService();
    when(taskRepository.save(any(Task.class))).thenThrow(new RuntimeException("persist failed"));

    ResponseEntity<String> response =
        restTemplate.postForEntity("/api/tasks", jsonEntity(createDto("bad")), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void updateTask_returnsOk() {
    warmUpTaskService();
    TaskResponseDto created =
        restTemplate
            .postForEntity("/api/tasks", jsonEntity(createDto("old")), TaskResponseDto.class)
            .getBody();
    assertThat(created).isNotNull();

    TaskUpdateDto body = new TaskUpdateDto();
    body.setTitle("upd");
    body.setDescription("d");
    body.setCompleted(true);

    ResponseEntity<TaskResponseDto> response =
        restTemplate.exchange(
            "/api/tasks/" + created.getId(), HttpMethod.PUT, jsonEntity(body), TaskResponseDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("upd");
  }

  @Test
  void updateTask_whenInvalidIdPath_returnsClientOrServerError() {
    TaskUpdateDto body = new TaskUpdateDto();
    body.setTitle("valid");

    ResponseEntity<String> response =
        restTemplate.exchange("/api/tasks/not-id", HttpMethod.PUT, jsonEntity(body), String.class);

    assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError())
        .isTrue();
  }

  @Test
  void updateTask_whenSaveFails_returnsInternalServerError() {
    warmUpTaskService();
    TaskResponseDto existing =
        restTemplate
            .postForEntity("/api/tasks", jsonEntity(createDto("tmp")), TaskResponseDto.class)
            .getBody();
    assertThat(existing).isNotNull();

    when(taskRepository.save(any(Task.class))).thenThrow(new RuntimeException("update failed"));
    TaskUpdateDto body = new TaskUpdateDto();
    body.setTitle("ups");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/tasks/" + existing.getId(), HttpMethod.PUT, jsonEntity(body), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void deleteTask_returnsNoContent() {
    warmUpTaskService();
    TaskResponseDto created =
        restTemplate
            .postForEntity("/api/tasks", jsonEntity(createDto("del")), TaskResponseDto.class)
            .getBody();
    assertThat(created).isNotNull();

    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/api/tasks/" + created.getId(), HttpMethod.DELETE, null, Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(taskRepository).deleteById(created.getId());
  }

  @Test
  void deleteTask_whenDeleteFails_returnsInternalServerError() {
    warmUpTaskService();
    doAnswer(
            inv -> {
              throw new RuntimeException("delete failed");
            })
        .when(taskRepository)
        .deleteById(anyLong());

    ResponseEntity<String> response =
        restTemplate.exchange("/api/tasks/1", HttpMethod.DELETE, null, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static <T> HttpEntity<T> jsonEntity(T body) {
    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }

  private static TaskCreateDto createDto(String title) {
    TaskCreateDto d = new TaskCreateDto();
    d.setTitle(title);
    d.setDescription("d");
    d.setDueDate(LocalDate.now().plusDays(1));
    d.setPriority(Priority.MEDIUM);
    d.setTags(new LinkedHashSet<>(Set.of("t")));
    return d;
  }
}
