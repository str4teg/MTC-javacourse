package com.mipt.andreysofronov.repository;

import com.mipt.andreysofronov.model.TaskAttachment;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class InMemoryTaskAttachmentRepository implements TaskAttachmentRepository {

  private final Map<Long, TaskAttachment> attachments = new ConcurrentHashMap<>();
  private final AtomicLong idSequence = new AtomicLong(0);

  @Override
  public TaskAttachment save(TaskAttachment attachment) {
    if (attachment == null) {
      throw new IllegalArgumentException("attachment must not be null");
    }
    Long id = attachment.getId();
    if (id == null) {
      id = idSequence.incrementAndGet();
      attachment.setId(id);
    }
    attachments.put(id, attachment);
    return attachment;
  }

  @Override
  public Optional<TaskAttachment> findById(Long id) {
    if (id == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(attachments.get(id));
  }

  @Override
  public List<TaskAttachment> findByTaskId(Long taskId) {
    if (taskId == null) {
      return List.of();
    }
    return attachments.values().stream()
        .filter(a -> taskId.equals(a.getTaskId()))
        .sorted(Comparator.comparing(TaskAttachment::getUploadedAt))
        .toList();
  }

  @Override
  public void deleteById(Long id) {
    if (id != null) {
      attachments.remove(id);
    }
  }
}
