package com.mipt.andreysofronov.gateway.api;

import com.mipt.andreysofronov.gateway.dto.CreateTaskRequest;
import com.mipt.andreysofronov.gateway.dto.TaskDto;
import com.mipt.andreysofronov.gateway.service.TasksGatewayService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Internal gateway endpoints over the external tasks service. All routes require authentication
 * (see {@code SecurityConfig}). The actual calls go through {@link TasksGatewayService}, which
 * applies rate limiting and the circuit breaker.
 */
@RestController
@RequestMapping("/api/v1")
public class TasksGatewayController {

  private final TasksGatewayService gateway;

  public TasksGatewayController(TasksGatewayService gateway) {
    this.gateway = gateway;
  }

  @PostMapping("/tasks")
  public ResponseEntity<TaskDto> create(@Valid @RequestBody CreateTaskRequest request) {
    TaskDto created = gateway.createTask(request);
    if (created.id() == null) {
      // Degraded path: upstream creation could not be confirmed.
      return ResponseEntity.accepted().body(created);
    }
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @GetMapping("/tasks/{id}")
  public ResponseEntity<TaskDto> get(@PathVariable Long id) {
    return ResponseEntity.ok(gateway.getTask(id));
  }

  @GetMapping("/tasks")
  public ResponseEntity<List<TaskDto>> list(
      @RequestParam(name = "completed", required = false) Boolean completed,
      @RequestParam(name = "limit", required = false) Integer limit) {
    return ResponseEntity.ok(gateway.listTasks(completed, limit));
  }

  @DeleteMapping("/tasks/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    gateway.deleteTask(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Debug hook to exercise resilience by hitting the emulator's unstable endpoint through the
   * resilient wrapper, e.g. {@code GET /api/v1/probe?mode=500} to drive the circuit breaker OPEN.
   */
  @GetMapping("/probe")
  public ResponseEntity<String> probe(@RequestParam(name = "mode") String mode) {
    return ResponseEntity.ok(gateway.probeUnstable(mode));
  }
}
