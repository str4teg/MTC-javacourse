package com.mipt.andreysofronov.gateway.external;

import com.mipt.andreysofronov.gateway.dto.CreateTaskRequest;
import com.mipt.andreysofronov.gateway.dto.TaskDto;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Emulator of the external tasks service ({@code /external/v1/**}) so the {@link
 * com.mipt.andreysofronov.gateway.client.ExternalTasksClient RestClient} and the resilience layer
 * can be exercised without a real upstream.
 *
 * <p>Supports CRUD with {@code 201 + Location}, {@code 204}, and {@code 404 + ProblemDetail}, plus
 * an {@code /unstable} endpoint that fakes timeouts, 500s, 429s and HTML 502s.
 */
@RestController
@RequestMapping("/external/v1")
public class ExternalApiController {

  private static final Logger log = LoggerFactory.getLogger(ExternalApiController.class);

  private final Map<Long, TaskDto> store = new ConcurrentHashMap<>();
  private final AtomicLong sequence = new AtomicLong(0);

  @PostMapping("/tasks")
  public ResponseEntity<TaskDto> create(@RequestBody CreateTaskRequest request) {
    long id = sequence.incrementAndGet();
    TaskDto task = new TaskDto(id, request.title(), request.completed());
    store.put(id, task);
    URI location = URI.create("/external/v1/tasks/" + id);
    return ResponseEntity.created(location).contentType(MediaType.APPLICATION_JSON).body(task);
  }

  @GetMapping("/tasks/{id}")
  public ResponseEntity<?> get(@PathVariable Long id) {
    TaskDto task = store.get(id);
    if (task == null) {
      return notFound(id);
    }
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(task);
  }

  @GetMapping("/tasks")
  public ResponseEntity<List<TaskDto>> list(
      @RequestParam(name = "completed", required = false) Boolean completed,
      @RequestParam(name = "limit", required = false) Integer limit) {

    List<TaskDto> result =
        store.values().stream()
            .filter(t -> completed == null || t.completed() == completed)
            .sorted(Comparator.comparingLong(TaskDto::id))
            .toList();
    if (limit != null && limit >= 0 && limit < result.size()) {
      result = result.subList(0, limit);
    }
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
  }

  @DeleteMapping("/tasks/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    TaskDto removed = store.remove(id);
    if (removed == null) {
      return notFound(id);
    }
    return ResponseEntity.noContent().build();
  }

  /** {@code mode = timeout | 500 | 429 | html}. */
  @GetMapping("/unstable")
  public ResponseEntity<?> unstable(@RequestParam(name = "mode") String mode)
      throws InterruptedException {
    switch (mode) {
      case "timeout" -> {
        // Sleep well beyond the client read timeout so the call aborts on the client side.
        Thread.sleep(10_000);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("\"late\"");
      }
      case "500" -> {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated upstream failure");
      }
      case "429" -> {
        ProblemDetail pd =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS, "Upstream is rate limiting the gateway");
        pd.setTitle("Too Many Requests");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .header(HttpHeaders.RETRY_AFTER, "2")
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(pd);
      }
      case "html" -> {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .contentType(MediaType.TEXT_HTML)
            .body("<html><body><h1>502 Bad Gateway</h1></body></html>");
      }
      default -> {
        return ResponseEntity.badRequest()
            .contentType(MediaType.APPLICATION_JSON)
            .body("\"unknown mode: " + mode + "\"");
      }
    }
  }

  private ResponseEntity<ProblemDetail> notFound(Long id) {
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Task " + id + " was not found");
    pd.setTitle("Task Not Found");
    pd.setProperty("taskId", id);
    log.debug("External emulator: task {} not found", id);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(pd);
  }

  private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setTitle(status.getReasonPhrase());
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(pd);
  }
}
