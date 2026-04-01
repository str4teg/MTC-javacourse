package com.mipt.andreysofronov.service;

import com.mipt.andreysofronov.exception.TaskNotFoundException;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.TaskRepository;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FavoritesService {

  /** Имя атрибута сессии со списком избранных идентификаторов задач. */
  public static final String SESSION_ATTR_FAVORITE_TASK_IDS = "favoriteTaskIds";

  private final TaskRepository taskRepository;

  public FavoritesService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public void addFavorite(HttpSession session, Long taskId) {
    taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    LinkedHashSet<Long> ids = getOrCreateFavoriteIds(session);
    ids.add(taskId);
  }

  public void removeFavorite(HttpSession session, Long taskId) {
    Object attr = session.getAttribute(SESSION_ATTR_FAVORITE_TASK_IDS);
    if (attr instanceof LinkedHashSet<?> set) {
      @SuppressWarnings("unchecked")
      LinkedHashSet<Long> ids = (LinkedHashSet<Long>) set;
      ids.remove(taskId);
    }
  }

  public List<Long> getFavoriteTaskIds(HttpSession session) {
    Object attr = session.getAttribute(SESSION_ATTR_FAVORITE_TASK_IDS);
    if (!(attr instanceof LinkedHashSet<?> set)) {
      return List.of();
    }
    return set.stream().map(id -> (Long) id).toList();
  }

  public List<Task> getFavoriteTasks(HttpSession session) {
    return getFavoriteTaskIds(session).stream()
        .map(taskRepository::findById)
        .flatMap(Optional::stream)
        .toList();
  }

  @SuppressWarnings("unchecked")
  private static LinkedHashSet<Long> getOrCreateFavoriteIds(HttpSession session) {
    Object attr = session.getAttribute(SESSION_ATTR_FAVORITE_TASK_IDS);
    if (attr instanceof LinkedHashSet<?> existing) {
      return (LinkedHashSet<Long>) existing;
    }
    LinkedHashSet<Long> ids = new LinkedHashSet<>();
    session.setAttribute(SESSION_ATTR_FAVORITE_TASK_IDS, ids);
    return ids;
  }
}
