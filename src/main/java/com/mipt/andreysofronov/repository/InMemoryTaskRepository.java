package com.mipt.andreysofronov.repository;

import com.mipt.andreysofronov.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class InMemoryTaskRepository implements TaskRepository {

  private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
  private final AtomicLong idSequence = new AtomicLong(0);

  @Override
  public Task save(Task task) {
    if (task == null) {
      throw new IllegalArgumentException("task must not be null");
    }
    Long id = task.getId();
    if (id == null) {
      id = idSequence.incrementAndGet();
      task.setId(id);
    }
    tasks.put(id, task);
    return task;
  }

  @Override
  public Optional<Task> findById(Long id) {
    if (id == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(tasks.get(id));
  }

  @Override
  public List<Task> findAll() {
    return new ArrayList<>(tasks.values());
  }

  @Override
  public void deleteById(Long id) {
    if (id != null) {
      tasks.remove(id);
    }
  }
}
