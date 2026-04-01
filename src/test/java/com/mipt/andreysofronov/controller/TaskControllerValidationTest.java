package com.mipt.andreysofronov.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.andreysofronov.dto.TaskCreateDto;
import com.mipt.andreysofronov.exception.GlobalExceptionHandler;
import com.mipt.andreysofronov.mapper.TaskMapper;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.repository.TaskRepository;
import com.mipt.andreysofronov.service.TaskService;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TaskController.class)
@Import(GlobalExceptionHandler.class)
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
class TaskControllerValidationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private TaskService taskService;
  @MockitoBean private TaskMapper taskMapper;
  /** Нужен для бина {@link com.mipt.andreysofronov.validation.DueDateNotBeforeCreationValidator} при валидации PUT. */
  @MockitoBean private TaskRepository taskRepository;

  @Test
  void createTask_whenTitleTooShort_returns400() throws Exception {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("ab");
    dto.setDescription("d");
    dto.setDueDate(LocalDate.now().plusDays(1));
    dto.setPriority(Priority.MEDIUM);
    dto.setTags(new LinkedHashSet<>(Set.of("t")));

    mockMvc
        .perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"));
  }

  @Test
  void createTask_whenMalformedJson_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void updateTask_whenTitleTooShort_returns400() throws Exception {
    String body = "{\"title\":\"a\",\"completed\":true}";

    mockMvc
        .perform(
            put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }
}
