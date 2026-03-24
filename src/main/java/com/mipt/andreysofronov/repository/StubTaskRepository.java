package com.mipt.andreysofronov.repository;

import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StubTaskRepository implements TaskRepository {

  private final Map<Long, Task> fixedTasks;

  public StubTaskRepository() {
    Map<Long, Task> map = new LinkedHashMap<>();

    Task task = new Task();
    task.setId(101L);
    task.setTitle("task");
    task.setDescription("task");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now());
    task.setDueDate(LocalDate.now().plusDays(1));
    task.setPriority(Priority.LOW);
    task.setTags(new LinkedHashSet<>(Set.of("stub")));
    map.put(task.getId(), task);

    this.fixedTasks = map;
  }

  @Override
  public Task save(Task task) {
    if (task == null) {
      throw new IllegalArgumentException("task must not be null");
    }
    return task;
  }

  @Override
  public Optional<Task> findById(Long id) {
    if (id == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(copyOf(fixedTasks.get(id)));
  }

  @Override
  public List<Task> findAll() {
    return fixedTasks.values().stream().map(this::copyOf).collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {}

  private Task copyOf(Task source) {
    if (source == null) {
      return null;
    }
    Task copy = new Task();
    copy.setId(source.getId());
    copy.setTitle(source.getTitle());
    copy.setDescription(source.getDescription());
    copy.setCompleted(source.isCompleted());
    copy.setCreatedAt(source.getCreatedAt());
    copy.setDueDate(source.getDueDate());
    copy.setPriority(source.getPriority());
    copy.setTags(
        source.getTags() == null
            ? new LinkedHashSet<>()
            : new LinkedHashSet<>(source.getTags()));
    return copy;
  }
}
