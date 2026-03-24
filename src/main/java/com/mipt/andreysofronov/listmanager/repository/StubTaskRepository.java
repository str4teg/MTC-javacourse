package com.mipt.andreysofronov.listmanager.repository;

import com.mipt.andreysofronov.listmanager.model.Task;
import java.util.LinkedHashMap;
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
  public void deleteById(Long id) {
    
  }

  private Task copyOf(Task source) {
    if (source == null) {
      return null;
    }
    Task copy = new Task();
    copy.setId(source.getId());
    copy.setTitle(source.getTitle());
    copy.setDescription(source.getDescription());
    copy.setCompleted(source.isCompleted());
    return copy;
  }
}
