package com.mipt.andreysofronov.dto;

import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.validation.DueDateNotBeforeCreation;
import com.mipt.andreysofronov.validation.OnUpdate;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Частичное обновление задачи (все поля опциональны)")
@DueDateNotBeforeCreation(groups = OnUpdate.class)
public class TaskUpdateDto {

  @Schema(description = "Заголовок", minLength = 3, maxLength = 100)
  @Size(min = 3, max = 100, groups = OnUpdate.class)
  private String title;

  @Schema(description = "Описание", maxLength = 500)
  @Size(max = 500, groups = OnUpdate.class)
  private String description;

  @Schema(description = "Признак выполнения")
  private Boolean completed;

  @Schema(description = "Срок выполнения (не раньше даты создания задачи)")
  private LocalDate dueDate;

  @Schema(description = "Приоритет")
  private Priority priority;

  @ArraySchema(
      arraySchema = @Schema(description = "Теги (не более 5)"),
      schema = @Schema(implementation = String.class),
      maxItems = 5)
  @Size(max = 5, groups = OnUpdate.class)
  private Set<String> tags;

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

  public Boolean getCompleted() {
    return completed;
  }

  public void setCompleted(Boolean completed) {
    this.completed = completed;
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
    this.tags = tags;
  }
}
