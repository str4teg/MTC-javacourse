package com.mipt.andreysofronov.dto;

import com.mipt.andreysofronov.model.Priority;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

public class TaskCreateDto {

  private String title;
  private String description;
  private LocalDate dueDate;
  private Priority priority;
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
