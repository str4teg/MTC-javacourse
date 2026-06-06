package com.mipt.andreysofronov.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mipt.andreysofronov.config.JpaAuditingConfig;
import com.mipt.andreysofronov.exception.TaskBulkCompletionException;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.JpaTaskRepositoryAdapter;
import com.mipt.andreysofronov.repository.TaskJpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({JpaAuditingConfig.class, TaskService.class, JpaTaskRepositoryAdapter.class})
class TaskServiceIntegrationTest {

  @Autowired private TaskService taskService;
  @Autowired private TaskJpaRepository taskJpaRepository;

  @Test
  void bulkCompleteTasks_rollsBackWhenSomeIdsMissing() {
    Task task = taskJpaRepository.save(task("rollback"));
    Long id = task.getId();

    assertThatThrownBy(() -> taskService.bulkCompleteTasks(List.of(id, 999L)))
        .isInstanceOf(TaskBulkCompletionException.class)
        .hasMessageContaining("999");

    Task reloaded = taskJpaRepository.findById(id).orElseThrow();
    assertThat(reloaded.isCompleted()).isFalse();
  }

  @Test
  void bulkCompleteTasks_marksAllExistingTasks() {
    Task first = taskJpaRepository.save(task("first"));
    Task second = taskJpaRepository.save(task("second"));

    taskService.bulkCompleteTasks(List.of(first.getId(), second.getId()));

    assertThat(taskJpaRepository.findById(first.getId()).orElseThrow().isCompleted()).isTrue();
    assertThat(taskJpaRepository.findById(second.getId()).orElseThrow().isCompleted()).isTrue();
  }

  private static Task task(String title) {
    Task task = new Task();
    task.setTitle(title);
    task.setDescription(title + "-desc");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now());
    task.setUpdatedAt(LocalDateTime.now());
    task.setDueDate(LocalDate.now().plusDays(2));
    task.setPriority(Priority.MEDIUM);
    task.setTags(new LinkedHashSet<>(Set.of("x")));
    return task;
  }
}




