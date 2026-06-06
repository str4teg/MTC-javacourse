package com.mipt.andreysofronov.service;

import com.mipt.andreysofronov.exception.TaskBulkCompletionException;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.TaskJpaRepository;
import com.mipt.andreysofronov.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskJpaRepository taskJpaRepository;

  public TaskService(TaskRepository taskRepository, TaskJpaRepository taskJpaRepository) {
    this.taskRepository = taskRepository;
    this.taskJpaRepository = taskJpaRepository;
  }

  @Transactional
  public Task save(Task task) {
    return taskRepository.save(task);
  }

  @Transactional(readOnly = true)
  public Optional<Task> findById(Long id) {
    return taskRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public List<Task> findAll() {
    return taskRepository.findAll();
  }

  @Transactional(readOnly = true)
  public List<Task> findAllWithAttachments() {
    return taskJpaRepository.findAllWithAttachments();
  }

  @Transactional(readOnly = true)
  public Optional<Task> findByIdWithAttachments(Long id) {
    return taskJpaRepository.findWithAttachmentsById(id);
  }

  @Transactional(
      propagation = Propagation.REQUIRED,
      isolation = Isolation.READ_COMMITTED,
      rollbackFor = TaskBulkCompletionException.class)
  public void bulkCompleteTasks(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }

    List<Long> distinctIds = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
    if (distinctIds.isEmpty()) {
      return;
    }

    List<Task> tasks = taskJpaRepository.findAllById(distinctIds);
    var tasksById = tasks.stream().collect(Collectors.toMap(Task::getId, Function.identity()));
    List<Long> missingIds =
        distinctIds.stream().filter(id -> !tasksById.containsKey(id)).toList();
    if (!missingIds.isEmpty()) {
      throw new TaskBulkCompletionException(missingIds);
    }

    tasks.forEach(task -> task.setCompleted(true));
    taskJpaRepository.saveAll(tasks);
  }

  @Transactional
  public void deleteById(Long id) {
    taskRepository.deleteById(id);
  }
}
