package com.mipt.andreysofronov.listmanager.controller;

import com.mipt.andreysofronov.listmanager.model.Task;
import com.mipt.andreysofronov.listmanager.service.TaskService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskRestController {

  private final TaskService taskService;

  public TaskRestController(TaskService taskService) {
    this.taskService = taskService;
  }

  @PostMapping
  public ResponseEntity<Task> create(@RequestBody Task task) {
    Task saved = taskService.save(task);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Task> readOne(@PathVariable Long id) {
    return taskService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping
  public List<Task> readAll() {
    return taskService.findAll();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Task> update(@PathVariable Long id, @RequestBody Task task) {
    task.setId(id);
    Task saved = taskService.save(task);
    return ResponseEntity.ok(saved);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    taskService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
