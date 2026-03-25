package com.mipt.andreysofronov.controller;

import com.mipt.andreysofronov.dto.ErrorResponse;
import com.mipt.andreysofronov.dto.TaskCreateDto;
import com.mipt.andreysofronov.dto.TaskResponseDto;
import com.mipt.andreysofronov.dto.TaskUpdateDto;
import com.mipt.andreysofronov.exception.TaskNotFoundException;
import com.mipt.andreysofronov.mapper.TaskMapper;
import com.mipt.andreysofronov.validation.OnCreate;
import com.mipt.andreysofronov.validation.OnUpdate;
import com.mipt.andreysofronov.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(name = "Tasks", description = "CRUD операции с задачами")
public class TaskController {

  private static final String HEADER_TOTAL_COUNT = "X-Total-Count";

  private final TaskService taskService;
  private final TaskMapper taskMapper;

  public TaskController(TaskService taskService, TaskMapper taskMapper) {
    this.taskService = taskService;
    this.taskMapper = taskMapper;
  }

  @GetMapping
  @Operation(summary = "Список всех задач")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Список задач; в заголовке X-Total-Count - общее число задач",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class)))),
    @ApiResponse(
        responseCode = "500",
        description = "Внутренняя ошибка",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
    List<TaskResponseDto> body =
        taskService.findAll().stream().map(taskMapper::toResponseDto).toList();
    return ResponseEntity.ok()
        .header(HEADER_TOTAL_COUNT, String.valueOf(body.size()))
        .body(body);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Получить задачу по идентификатору")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Найдена",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TaskResponseDto.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Задача не найдена",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<TaskResponseDto> getTaskById(
      @Parameter(description = "Идентификатор задачи", required = true) @PathVariable("id")
          Long id) {
    return ResponseEntity.ok(
        taskMapper.toResponseDto(
            taskService.findById(id).orElseThrow(() -> new TaskNotFoundException(id))));
  }

  @PostMapping
  @Operation(summary = "Создать задачу")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Создана",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TaskResponseDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Ошибка валидации",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<TaskResponseDto> createTask(
      @Validated(OnCreate.class) @RequestBody TaskCreateDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(taskMapper.toResponseDto(taskService.save(taskMapper.toEntity(dto))));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Обновить задачу")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Обновлена",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TaskResponseDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Ошибка валидации",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Задача не найдена",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<TaskResponseDto> updateTask(
      @Parameter(description = "Идентификатор задачи", required = true) @PathVariable("id")
          Long id,
      @Validated(OnUpdate.class) @RequestBody TaskUpdateDto dto) {
    var task = taskService.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    taskMapper.updateEntity(dto, task);
    return ResponseEntity.ok(taskMapper.toResponseDto(taskService.save(task)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Удалить задачу")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Удалена (идемпотентно)"),
    @ApiResponse(
        responseCode = "500",
        description = "Внутренняя ошибка",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<Void> deleteTask(
      @Parameter(description = "Идентификатор задачи", required = true) @PathVariable("id")
          Long id) {
    taskService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
