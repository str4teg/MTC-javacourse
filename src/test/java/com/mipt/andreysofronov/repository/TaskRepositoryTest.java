package com.mipt.andreysofronov.repository;

import com.mipt.andreysofronov.config.JpaAuditingConfig;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.model.TaskAttachment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class TaskRepositoryTest {

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository attachmentRepository;

  @Test
  void saveTaskWithAttachmentAndFindDueBetween() {
    Task t = new Task();
    t.setTitle("t1");
    t.setDescription("d");
    t.setCreatedAt(LocalDateTime.now());
    t.setDueDate(LocalDate.now().plusDays(3));
    t.setPriority(Priority.MEDIUM);
    t = taskRepository.save(t);

    TaskAttachment a = new TaskAttachment();
    a.setFileName("f.txt");
    a.setStoredFileName("s_f.txt");
    a.setContentType("text/plain");
    a.setSize(10);
    a.setUploadedAt(LocalDateTime.now());
    a.setTask(t);
    attachmentRepository.save(a);

    List<Task> due = taskRepository.findDueBetween(LocalDate.now(), LocalDate.now().plusDays(7));
    assertThat(due).isNotEmpty();

    List<Task> withAttachments = taskRepository.findAllWithAttachments();
    assertThat(withAttachments).isNotEmpty();
    assertThat(withAttachments.get(0).getAttachments()).isNotNull();
  }
}

