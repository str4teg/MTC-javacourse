package com.mipt.andreysofronov.repository;

import com.mipt.andreysofronov.model.TaskAttachment;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class JpaTaskAttachmentRepositoryAdapter implements TaskAttachmentRepository {

  private final TaskAttachmentJpaRepository delegate;

  public JpaTaskAttachmentRepositoryAdapter(TaskAttachmentJpaRepository delegate) {
    this.delegate = delegate;
  }

  @Override
  public TaskAttachment save(TaskAttachment attachment) {
    return delegate.save(attachment);
  }

  @Override
  public Optional<TaskAttachment> findById(Long id) {
    return delegate.findById(id);
  }

  @Override
  public List<TaskAttachment> findByTaskId(Long taskId) {
    return delegate.findByTaskId(taskId);
  }

  @Override
  public void deleteById(Long id) {
    delegate.deleteById(id);
  }
}

