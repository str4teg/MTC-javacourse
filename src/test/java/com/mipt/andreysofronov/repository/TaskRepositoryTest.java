package com.mipt.andreysofronov.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mipt.andreysofronov.config.JpaAuditingConfig;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.model.TaskAttachment;
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
@Import(JpaAuditingConfig.class)
class TaskRepositoryTest {

  @Autowired private TaskJpaRepository taskRepository;
  @Autowired private TaskAttachmentJpaRepository attachmentRepository;

  @Test
  void findByCompletedAndPriority_filtersTasks() {
	taskRepository.saveAll(
		List.of(
			task("done-high", true, Priority.HIGH, LocalDate.now().plusDays(1)),
			task("todo-high", false, Priority.HIGH, LocalDate.now().plusDays(2)),
			task("todo-low", false, Priority.LOW, LocalDate.now().plusDays(3))));

	List<Task> result = taskRepository.findByCompletedAndPriority(false, Priority.HIGH);

	assertThat(result).hasSize(1);
	assertThat(result.get(0).getTitle()).isEqualTo("todo-high");
  }

  @Test
  void findDueWithinNextSevenDays_returnsOnlyTasksInsideWindow() {
	taskRepository.saveAll(
		List.of(
			task("soon", false, Priority.MEDIUM, LocalDate.now().plusDays(3)),
			task("boundary", false, Priority.MEDIUM, LocalDate.now().plusDays(7)),
			task("late", false, Priority.MEDIUM, LocalDate.now().plusDays(8))));

	List<Task> result = taskRepository.findDueWithinNextSevenDays(LocalDate.now().plusDays(7));

	assertThat(result).extracting(Task::getTitle).containsExactly("soon", "boundary");
  }

  @Test
  void saveTaskWithAttachment_persistsForeignKeyAndLoadsByTaskId() {
	Task task = taskRepository.save(task("task", false, Priority.LOW, LocalDate.now().plusDays(4)));

	TaskAttachment attachment = new TaskAttachment();
	attachment.setTask(task);
	attachment.setTaskId(task.getId());
	attachment.setFileName("note.txt");
	attachment.setStoredFileName("stored-note.txt");
	attachment.setContentType("text/plain");
	attachment.setSize(5L);
	attachment.setUploadedAt(LocalDateTime.now());
	attachmentRepository.saveAndFlush(attachment);

	List<TaskAttachment> attachments = attachmentRepository.findByTaskId(task.getId());

	assertThat(attachments).hasSize(1);
	assertThat(attachments.get(0).getTaskId()).isEqualTo(task.getId());
	assertThat(attachments.get(0).getFileName()).isEqualTo("note.txt");
  }

  private static Task task(String title, boolean completed, Priority priority, LocalDate dueDate) {
	Task task = new Task();
	task.setTitle(title);
	task.setDescription(title + "-desc");
	task.setCompleted(completed);
	task.setCreatedAt(LocalDateTime.now());
	task.setUpdatedAt(LocalDateTime.now());
	task.setDueDate(dueDate);
	task.setPriority(priority);
	task.setTags(new LinkedHashSet<>(Set.of("tag")));
	return task;
  }
}

