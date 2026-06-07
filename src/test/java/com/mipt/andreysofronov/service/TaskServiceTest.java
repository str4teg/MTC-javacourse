package com.mipt.andreysofronov.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.TaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Часть 1: модульное тестирование сервисного слоя (Лондонская / mockist школа).
 *
 * <p>Слой доступа к данным изолирован моком {@link TaskRepository}, проверяется не только результат,
 * но и взаимодействие сервиса с абстракцией репозитория (interaction verification через
 * {@code verify} + {@link ArgumentCaptor}).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TaskServiceTest {

  @Autowired private TaskService taskService;

  /**
   * Активный бин {@link TaskRepository} — это {@code @Primary}
   * {@link com.mipt.andreysofronov.repository.JpaTaskRepositoryAdapter}, зарегистрированный под
   * именем {@code inMemoryTaskRepository}. Подменяем именно его моком.
   */
  @MockitoBean(name = "inMemoryTaskRepository")
  private TaskRepository taskRepository;

  @Test
  void updateStatusOfExistingTask_savesCompletedTask_andVerifiesRepositoryInteraction() {
    // given: в репозитории есть невыполненная задача
    long taskId = 1L;
    Task existing = task(taskId, "Прочитать книгу", false);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when: меняем статус существующей задачи на "выполнена" и сохраняем через сервис
    Task loaded = taskService.findById(taskId).orElseThrow();
    loaded.setCompleted(true);
    Task result = taskService.save(loaded);

    // then: проверяем результат
    assertThat(result.getId()).isEqualTo(taskId);
    assertThat(result.isCompleted()).isTrue();

    // then: проверяем взаимодействие с репозиторием (что именно ушло в save)
    ArgumentCaptor<Task> savedCaptor = ArgumentCaptor.forClass(Task.class);
    verify(taskRepository).findById(taskId);
    verify(taskRepository).save(savedCaptor.capture());
    assertThat(savedCaptor.getValue().getId()).isEqualTo(taskId);
    assertThat(savedCaptor.getValue().isCompleted()).isTrue();
    verifyNoMoreInteractions(taskRepository);
  }

  private static Task task(Long id, String title, boolean completed) {
    Task task = new Task();
    task.setId(id);
    task.setTitle(title);
    task.setDescription(title + "-desc");
    task.setCompleted(completed);
    task.setCreatedAt(LocalDateTime.now());
    task.setUpdatedAt(LocalDateTime.now());
    task.setDueDate(LocalDate.now().plusDays(2));
    task.setPriority(Priority.MEDIUM);
    task.setTags(new LinkedHashSet<>(Set.of("tag")));
    return task;
  }
}
