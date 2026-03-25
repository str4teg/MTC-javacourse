package com.mipt.andreysofronov.dto;

import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.validation.OnCreate;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Schema(description = "Тело запроса при создании задачи")
public class TaskCreateDto {

  @Schema(description = "Заголовок", minLength = 3, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(groups = OnCreate.class)
  @Size(min = 3, max = 100, groups = OnCreate.class)
  private String title;

  @Schema(description = "Описание", maxLength = 500)
  @Size(max = 500, groups = OnCreate.class)
  private String description;

  @Schema(description = "Срок выполнения (не в прошлом)", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(groups = OnCreate.class)
  @FutureOrPresent(groups = OnCreate.class)
  private LocalDate dueDate;

  @Schema(description = "Приоритет", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(groups = OnCreate.class)
  private Priority priority;

  @ArraySchema(
      arraySchema = @Schema(description = "Теги (не более 5)"),
      schema = @Schema(implementation = String.class),
      maxItems = 5)
  @Size(max = 5, groups = OnCreate.class)
  private Set<String> tags = new LinkedHashSet<>();

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
