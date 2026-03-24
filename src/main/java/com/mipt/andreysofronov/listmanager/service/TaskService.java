package com.mipt.andreysofronov.listmanager.service;

import com.mipt.andreysofronov.listmanager.model.Task;
import com.mipt.andreysofronov.listmanager.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
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

  public void deleteById(Long id) {
    taskRepository.deleteById(id);
  }
}
