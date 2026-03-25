package com.mipt.andreysofronov.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.TaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskStatisticsServiceTest {

  @Mock(name = "primaryTaskRepository")
  private TaskRepository primaryTaskRepository;

  @Mock(name = "stubTaskRepository")
  private TaskRepository stubTaskRepository;

  private TaskStatisticsService taskStatisticsService;

  @BeforeEach
  void setUp() {
    taskStatisticsService = new TaskStatisticsService(primaryTaskRepository, stubTaskRepository);
  }

  @Test
  void getComparisonSummary_includesBothRepositorySizes() {
    when(primaryTaskRepository.findAll()).thenReturn(List.of(sampleTask(1L)));
    when(stubTaskRepository.findAll()).thenReturn(List.of(sampleTask(2L), sampleTask(3L)));

    String summary = taskStatisticsService.getComparisonSummary();

    assertThat(summary).contains("1 задач").contains("2 задач");
  }

  @Test
  void explainFindById_describesBothRepositories() {
    Task t = sampleTask(5L);
    when(primaryTaskRepository.findById(5L)).thenReturn(Optional.of(t));
    when(stubTaskRepository.findById(5L)).thenReturn(Optional.empty());

    String text = taskStatisticsService.explainFindById(5L);

    assertThat(text).contains("id=5").contains("основном").contains("заглушке");
  }

  @Test
  void listIdsSideBySide_formatsIds() {
    when(primaryTaskRepository.findAll()).thenReturn(List.of(sampleTask(10L)));
    when(stubTaskRepository.findAll()).thenReturn(List.of(sampleTask(20L)));

    String text = taskStatisticsService.listIdsSideBySide();

    assertThat(text).contains("[10]").contains("[20]");
  }

  private static Task sampleTask(Long id) {
    Task t = new Task();
    t.setId(id);
    t.setTitle("t" + id);
    t.setDescription("d");
    t.setCompleted(false);
    t.setCreatedAt(LocalDateTime.now());
    t.setDueDate(LocalDate.now().plusDays(1));
    t.setPriority(Priority.MEDIUM);
    t.setTags(new LinkedHashSet<>(Set.of("x")));
    return t;
  }
}
