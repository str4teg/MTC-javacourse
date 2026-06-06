package com.mipt.andreysofronov.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mipt.andreysofronov.dto.TaskResponseDto;
import com.mipt.andreysofronov.exception.GlobalExceptionHandler;
import com.mipt.andreysofronov.exception.TaskNotFoundException;
import com.mipt.andreysofronov.mapper.TaskMapper;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.service.FavoritesService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
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

@WebMvcTest(controllers = FavoritesController.class)
@AutoConfigureMockMvc(addFilters = false)
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
class FavoritesControllerWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FavoritesService favoritesService;
  @MockitoBean private TaskMapper taskMapper;

  @Test
  void getFavoriteTasks_returns200WithList() throws Exception {
    Task task = sampleTask(1L);
    TaskResponseDto dto = sampleDto(1L);
    when(favoritesService.getFavoriteTasks(any())).thenReturn(List.of(task));
    when(taskMapper.toResponseDto(task)).thenReturn(dto);

    mockMvc
        .perform(get("/api/favorites").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Total-Count", "1"))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].title").value("t1"));
  }

  @Test
  void addFavorite_whenOk_returns204() throws Exception {
    mockMvc.perform(post("/api/favorites/5")).andExpect(status().isNoContent());

    verify(favoritesService).addFavorite(any(), eq(5L));
  }

  @Test
  void addFavorite_whenTaskNotFound_returns404() throws Exception {
    doThrow(new TaskNotFoundException(99L)).when(favoritesService).addFavorite(any(), eq(99L));

    mockMvc
        .perform(post("/api/favorites/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void removeFavorite_returns204() throws Exception {
    mockMvc.perform(delete("/api/favorites/3")).andExpect(status().isNoContent());

    verify(favoritesService).removeFavorite(any(), eq(3L));
  }

  private static Task sampleTask(Long id) {
    Task t = new Task();
    t.setId(id);
    t.setTitle("t" + id);
    t.setDescription("d");
    t.setCompleted(false);
    t.setCreatedAt(LocalDateTime.now());
    t.setDueDate(LocalDate.now().plusDays(1));
    t.setPriority(Priority.MEDIUM);
    t.setTags(new LinkedHashSet<>(Set.of("x")));
    return t;
  }

  private static TaskResponseDto sampleDto(long id) {
    TaskResponseDto d = new TaskResponseDto();
    d.setId(id);
    d.setTitle("t" + id);
    d.setDescription("d");
    d.setCompleted(false);
    d.setCreatedAt(LocalDateTime.now());
    d.setDueDate(LocalDate.now().plusDays(1));
    d.setPriority(Priority.MEDIUM);
    d.setTags(new LinkedHashSet<>(Set.of("x")));
    return d;
  }
}
