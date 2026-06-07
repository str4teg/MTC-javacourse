package com.mipt.andreysofronov.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.andreysofronov.dto.TaskCreateDto;
import com.mipt.andreysofronov.exception.GlobalExceptionHandler;
import com.mipt.andreysofronov.mapper.TaskMapperImpl;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.TaskRepository;
import com.mipt.andreysofronov.service.TaskService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Часть 2: slice-тестирование web-слоя через {@link WebMvcTest} + {@link MockMvc}.
 *
 * <p>Поднимается только web-контекст для {@link TaskController}; {@link TaskService} заменён моком,
 * а реальный {@link TaskMapperImpl} импортируется, чтобы проверять настоящую сериализацию сущности
 * в JSON. Покрыты коды статусов 201 (создание) и 200 (получение по id), валидация входных данных и
 * формат JSON-ответа.
 */
@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, TaskMapperImpl.class})
@TestPropertySource(
    properties = {
      "spring.profiles.active=test",
      "app.name=test-app",
      "app.version=0-test",
      "app.api.version=2.0.0",
      "openapi.title=T",
      "openapi.description=D",
      "openapi.contact.name=N",
      "openapi.contact.email=n@e.com"
    })
class TaskControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private TaskService taskService;

  /** Нужен для бина {@link com.mipt.andreysofronov.validation.DueDateNotBeforeCreationValidator}. */
  @MockitoBean private TaskRepository taskRepository;

  @Test
  void createTask_returnsCreatedWithJsonBody() throws Exception {
    // given: валидный запрос на создание; сервис «сохраняет» сущность и проставляет ей id
    TaskCreateDto dto = createDto("Купить молоко");
    when(taskService.save(any(Task.class)))
        .thenAnswer(
            invocation -> {
              Task toSave = invocation.getArgument(0);
              toSave.setId(99L);
              return toSave;
            });

    // when / then: 201 Created + тело ответа в формате JSON отражает сохранённую задачу
    mockMvc
        .perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(99))
        .andExpect(jsonPath("$.title").value("Купить молоко"))
        .andExpect(jsonPath("$.description").value("Купить молоко-desc"))
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.priority").value("MEDIUM"))
        .andExpect(jsonPath("$.tags[0]").value("tag"));
  }

  @Test
  void getTaskById_whenFound_returnsOkWithJsonBody() throws Exception {
    // given: в сервисе лежит ранее сохранённая задача
    Task saved = task(7L, "Прочитать книгу", true);
    when(taskService.findById(7L)).thenReturn(Optional.of(saved));

    // when / then: 200 OK + JSON-тело совпадает с сохранённой задачей
    mockMvc
        .perform(get("/api/tasks/{id}", 7L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.title").value("Прочитать книгу"))
        .andExpect(jsonPath("$.completed").value(true))
        .andExpect(jsonPath("$.priority").value("MEDIUM"));
  }

  @Test
  void createTask_whenTitleTooShort_returnsBadRequestJson() throws Exception {
    // given: заголовок короче минимума (3 символа) — нарушение валидации
    TaskCreateDto dto = createDto("ab");

    // when / then: 400 Bad Request с телом ошибки в формате JSON
    mockMvc
        .perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"));
  }

  private static Task task(Long id, String title, boolean completed) {
    Task task = new Task();
    task.setId(id);
    task.setTitle(title);
    task.setDescription(title + "-desc");
    task.setCompleted(completed);
    task.setCreatedAt(LocalDateTime.now());
    task.setUpdatedAt(LocalDateTime.now());
    task.setDueDate(LocalDate.now().plusDays(1));
    task.setPriority(Priority.MEDIUM);
    task.setTags(new LinkedHashSet<>(Set.of("tag")));
    return task;
  }

  private static TaskCreateDto createDto(String title) {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle(title);
    dto.setDescription(title + "-desc");
    dto.setDueDate(LocalDate.now().plusDays(1));
    dto.setPriority(Priority.MEDIUM);
    dto.setTags(new LinkedHashSet<>(Set.of("tag")));
    return dto;
  }
}
