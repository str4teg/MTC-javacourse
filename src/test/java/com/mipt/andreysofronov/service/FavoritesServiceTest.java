package com.mipt.andreysofronov.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mipt.andreysofronov.exception.TaskNotFoundException;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.TaskRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FavoritesServiceTest {

  @Mock private TaskRepository taskRepository;
  @Mock private HttpSession session;

  private FavoritesService favoritesService;

  @BeforeEach
  void setUp() {
    favoritesService = new FavoritesService(taskRepository);
  }

  @Test
  @SuppressWarnings("unchecked")
  void addFavorite_whenTaskExists_addsIdToSession() {
    when(taskRepository.findById(7L)).thenReturn(Optional.of(sampleTask(7L)));
    when(session.getAttribute(FavoritesService.SESSION_ATTR_FAVORITE_TASK_IDS)).thenReturn(null);

    favoritesService.addFavorite(session, 7L);

    ArgumentCaptor<LinkedHashSet<Long>> captor = ArgumentCaptor.forClass(LinkedHashSet.class);
    verify(session).setAttribute(eq(FavoritesService.SESSION_ATTR_FAVORITE_TASK_IDS), captor.capture());
    assertThat(captor.getValue()).containsExactly(7L);
  }

  @Test
  void addFavorite_whenTaskMissing_throws() {
    when(taskRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> favoritesService.addFavorite(session, 99L))
        .isInstanceOf(TaskNotFoundException.class)
        .hasFieldOrPropertyWithValue("taskId", 99L);
  }

  @Test
  void getFavoriteTaskIds_returnsEmptyWhenNoAttribute() {
    when(session.getAttribute(FavoritesService.SESSION_ATTR_FAVORITE_TASK_IDS)).thenReturn(null);

    assertThat(favoritesService.getFavoriteTaskIds(session)).isEmpty();
  }

  @Test
  void getFavoriteTasks_resolvesTasksFromRepository() {
    LinkedHashSet<Long> ids = new LinkedHashSet<>(List.of(1L, 2L));
    when(session.getAttribute(FavoritesService.SESSION_ATTR_FAVORITE_TASK_IDS)).thenReturn(ids);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask(1L)));
    when(taskRepository.findById(2L)).thenReturn(Optional.empty());

    List<Task> tasks = favoritesService.getFavoriteTasks(session);

    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getId()).isEqualTo(1L);
  }

  @Test
  void removeFavorite_removesIdFromExistingSet() {
    LinkedHashSet<Long> ids = new LinkedHashSet<>(List.of(1L, 2L));
    when(session.getAttribute(FavoritesService.SESSION_ATTR_FAVORITE_TASK_IDS)).thenReturn(ids);

    favoritesService.removeFavorite(session, 1L);

    assertThat(ids).containsExactly(2L);
  }

  @Test
  void removeFavorite_whenNoSessionAttribute_doesNothing() {
    when(session.getAttribute(FavoritesService.SESSION_ATTR_FAVORITE_TASK_IDS)).thenReturn(null);

    favoritesService.removeFavorite(session, 1L);
  }

  private static Task sampleTask(Long id) {
    Task t = new Task();
    t.setId(id);
    t.setTitle("title");
    t.setDescription("d");
    t.setCompleted(false);
    t.setCreatedAt(LocalDateTime.now());
    t.setDueDate(LocalDate.now().plusDays(1));
    t.setPriority(Priority.MEDIUM);
    t.setTags(new LinkedHashSet<>(Set.of("t")));
    return t;
  }
}
