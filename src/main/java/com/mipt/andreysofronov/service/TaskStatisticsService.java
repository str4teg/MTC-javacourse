package com.mipt.andreysofronov.service;

import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Сравнение основного репозитория ({@code @Primary}) и заглушки ({@code @Qualifier}) для учебной демонстрации DI.
 */
@Service
public class TaskStatisticsService {

  private final TaskRepository primaryTaskRepository;
  private final TaskRepository stubTaskRepository;

  public TaskStatisticsService(
      TaskRepository primaryTaskRepository,
      @Qualifier("stubTaskRepository") TaskRepository stubTaskRepository) {
    this.primaryTaskRepository = primaryTaskRepository;
    this.stubTaskRepository = stubTaskRepository;
  }

  public String getComparisonSummary() {
    int primaryCount = primaryTaskRepository.findAll().size();
    int stubCount = stubTaskRepository.findAll().size();
    return String.format(
        "Основной репозиторий (%s, выбирается через @Primary): %d задач; "
            + "Заглушка (%s, @Qualifier(\"stubTaskRepository\")): %d задач",
        primaryTaskRepository.getClass().getSimpleName(),
        primaryCount,
        stubTaskRepository.getClass().getSimpleName(),
        stubCount);
  }

  public String explainFindById(Long id) {
    Optional<Task> fromPrimary = primaryTaskRepository.findById(id);
    Optional<Task> fromStub = stubTaskRepository.findById(id);
    return String.format(
        "id=%s: в основном=%s, в заглушке=%s",
        id,
        fromPrimary.map(t -> "'" + t.getTitle() + "'").orElse("нет"),
        fromStub.map(t -> "'" + t.getTitle() + "'").orElse("нет"));
  }

  public String listIdsSideBySide() {
    List<Long> primaryIds =
        primaryTaskRepository.findAll().stream()
            .map(Task::getId)
            .collect(Collectors.toList());
    List<Long> stubIds =
        stubTaskRepository.findAll().stream()
            .map(Task::getId)
            .collect(Collectors.toList());
    return String.format(
        "ids в основном %s; ids в заглушке %s",
        primaryIds,
        stubIds);
  }
}
