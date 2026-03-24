package com.mipt.andreysofronov.listmanager.repository;

import com.mipt.andreysofronov.listmanager.model.Task;
import java.util.List;
import java.util.Optional;

public interface        TaskRepository {

  Task save(Task task);

  Optional<Task> findById(Long id);

  List<Task> findAll();

  void deleteById(Long id);
}
