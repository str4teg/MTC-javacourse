package com.mipt.andreysofronov.service;

import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import com.mipt.andreysofronov.model.Priority;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private static final Logger log = LoggerFactory.getLogger(TaskService.class);

  private final TaskRepository taskRepository;
  private final Map<String, Task> taskCache = new ConcurrentHashMap<>();

  @SuppressWarnings("unused")
  private final String appName;

  @SuppressWarnings("unused")
  private final String appVersion;

  public TaskService(
      TaskRepository taskRepository,
      @Value("${app.name}") String appName,
      @Value("${app.version}") String appVersion) {
    this.taskRepository = taskRepository;
    this.appName = appName;
    this.appVersion = appVersion;
  }

  @PostConstruct
  public void initCache() {
    seedRepositoryWithPredefinedTasks();
    taskCache.clear();
    for (Task task : taskRepository.findAll()) {
      taskCache.put(String.valueOf(task.getId()), task);
    }
    log.info("Кэш задач инициализирован: {} записей", taskCache.size());
  }

  @PreDestroy
  public void cleanupCache() {
    int size = taskCache.size();
    log.info("Завершение работы TaskService: в кэше {} задач", size);
    Path statsFile =
        Path.of(System.getProperty("java.io.tmpdir"), "task-service-cache-stats.txt");
    try {
      String line =
          Instant.now() + " cacheTasks=" + size + System.lineSeparator();
      Files.writeString(
          statsFile,
          line,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    } catch (IOException e) {
      log.warn("Не удалось записать статистику кэша в {}: {}", statsFile, e.getMessage());
    }
  }

  private void seedRepositoryWithPredefinedTasks() {
    taskRepository.save(task("Кэш: встреча", "Обсудить спринт", false));
    taskRepository.save(task("Кэш: код-ревью", "Проверить PR #42", false));
    taskRepository.save(task("Кэш: документация", "Обновить README", true));
  }

  private static Task task(String title, String description, boolean completed) {
    Task t = new Task();
    t.setTitle(title);
    t.setDescription(description);
    t.setCompleted(completed);
    t.setCreatedAt(LocalDateTime.now());
    t.setDueDate(LocalDate.now().plusDays(7));
    t.setPriority(Priority.MEDIUM);
    t.setTags(new LinkedHashSet<>(Set.of("seed")));
    return t;
  }

  public Task save(Task task) {
    return taskRepository.save(task);
  }

  public Optional<Task> findById(Long id) {
    return taskRepository.findById(id);
  }

  public List<Task> findAll() {
    return taskRepository.findAll();
  }

  /**
   * Обновляет список задач как выполненные в рамках транзакции.
   * При отсутствии хотя бы одной задачи бросает TaskNotFoundException и откатывает всё.
   */
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = RuntimeException.class)
  public void bulkCompleteTasks(List<Long> ids) {
    for (Long id : ids) {
      Task t = taskRepository.findById(id).orElseThrow(() -> new com.mipt.andreysofronov.exception.TaskNotFoundException(id));
      t.setCompleted(true);
      taskRepository.save(t);
    }
  }

  public List<Task> findAllWithAttachments() {
    return taskRepository.findAllWithAttachments();
  }

  public void deleteById(Long id) {
    taskRepository.deleteById(id);
  }
}
