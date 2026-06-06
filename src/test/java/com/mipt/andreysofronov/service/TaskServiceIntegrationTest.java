package com.mipt.andreysofronov.service;

import com.mipt.andreysofronov.config.JpaAuditingConfig;
import com.mipt.andreysofronov.exception.TaskNotFoundException;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.TaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
public class TaskServiceIntegrationTest {

  @Autowired
  private TaskService taskService;

  @Autowired
  private TaskRepository taskRepository;

  @Test
  void bulkCompleteTasks_shouldRollbackOnMissingId() {
    Task t1 = new Task();
    t1.setTitle("t1");
    t1.setDescription("d1");
    t1.setCreatedAt(LocalDateTime.now());
    t1.setDueDate(LocalDate.now().plusDays(1));
    t1.setPriority(Priority.LOW);
    t1 = taskRepository.save(t1);

    Task t2 = new Task();
    t2.setTitle("t2");
    t2.setDescription("d2");
    t2.setCreatedAt(LocalDateTime.now());
    t2.setDueDate(LocalDate.now().plusDays(2));
    t2.setPriority(Priority.MEDIUM);
    t2 = taskRepository.save(t2);

    Long missingId = 99999L;
    Assertions.assertThrows(TaskNotFoundException.class, () -> taskService.bulkCompleteTasks(List.of(t1.getId(), missingId, t2.getId())));

    // всё должно остаться в прежнем состоянии (не выполнено)
    Task fresh1 = taskRepository.findById(t1.getId()).orElseThrow();
    Task fresh2 = taskRepository.findById(t2.getId()).orElseThrow();
    Assertions.assertFalse(fresh1.isCompleted());
    Assertions.assertFalse(fresh2.isCompleted());
  }
}

