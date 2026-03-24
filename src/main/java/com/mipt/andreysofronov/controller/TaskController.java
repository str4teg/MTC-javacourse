package com.mipt.andreysofronov.controller;

import com.mipt.andreysofronov.dto.TaskCreateDto;
import com.mipt.andreysofronov.dto.TaskResponseDto;
import com.mipt.andreysofronov.dto.TaskUpdateDto;
import com.mipt.andreysofronov.mapper.TaskMapper;
import com.mipt.andreysofronov.validation.OnCreate;
import com.mipt.andreysofronov.validation.OnUpdate;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.service.TaskService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
public class TaskController {

  private final TaskService taskService;
  private final TaskMapper taskMapper;

  public TaskController(TaskService taskService, TaskMapper taskMapper) {
    this.taskService = taskService;
    this.taskMapper = taskMapper;
  }

  @GetMapping
  public List<TaskResponseDto> getAllTasks() {
    return taskService.findAll().stream().map(taskMapper::toResponseDto).toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable("id") Long id) {
    return taskService
        .findById(id)
        .map(taskMapper::toResponseDto)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<TaskResponseDto> createTask(
      @Validated(OnCreate.class) @RequestBody TaskCreateDto dto) {
    Task saved = taskService.save(taskMapper.toEntity(dto));
    return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toResponseDto(saved));
  }

  @PutMapping("/{id}")
  public ResponseEntity<TaskResponseDto> updateTask(
      @PathVariable("id") Long id,
      @Validated(OnUpdate.class) @RequestBody TaskUpdateDto dto) {
    return taskService
        .findById(id)
        .map(
            task -> {
              taskMapper.updateEntity(dto, task);
              return taskMapper.toResponseDto(taskService.save(task));
            })
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id) {
    taskService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
