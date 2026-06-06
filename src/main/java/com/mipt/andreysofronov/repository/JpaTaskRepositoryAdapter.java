package com.mipt.andreysofronov.repository;

import com.mipt.andreysofronov.model.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("inMemoryTaskRepository")
@Primary
public class JpaTaskRepositoryAdapter implements TaskRepository {

  private final TaskJpaRepository delegate;

  public JpaTaskRepositoryAdapter(TaskJpaRepository delegate) {
    this.delegate = delegate;
  }

  @Override
  public Task save(Task task) {
    return delegate.save(task);
  }

  @Override
  public Optional<Task> findById(Long id) {
    return delegate.findById(id);
  }

  @Override
  public List<Task> findAll() {
    return delegate.findAll();
  }

  @Override
  public void deleteById(Long id) {
    delegate.deleteById(id);
  }
}


