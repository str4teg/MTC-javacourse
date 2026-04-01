package com.mipt.andreysofronov.dto;

import com.mipt.andreysofronov.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Schema(description = "Задача в ответе API")
public class TaskResponseDto {

  @Schema(description = "Идентификатор", example = "1")
  private Long id;

  @Schema(description = "Заголовок")
  private String title;

  @Schema(description = "Описание")
  private String description;

  @Schema(description = "Выполнена")
  private boolean completed;

  @Schema(description = "Дата и время создания")
  private LocalDateTime createdAt;

  @Schema(description = "Срок выполнения")
  private LocalDate dueDate;

  @Schema(description = "Приоритет")
  private Priority priority;

  @Schema(description = "Теги")
  private Set<String> tags = new LinkedHashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tags);
  }
}
