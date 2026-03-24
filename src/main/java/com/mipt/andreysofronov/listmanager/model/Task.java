package com.mipt.andreysofronov.listmanager.model;

import java.util.Objects;

public class Task {

  private Long id;
  private String title;
  private String description;
  private boolean completed;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Task task = (Task) o;
    return completed == task.completed
        && Objects.equals(id, task.id)
        && Objects.equals(title, task.title)
        && Objects.equals(description, task.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, completed);
  }

  @Override
  public String toString() {
    return "Task{"
        + "id=" + id
        + ", title='" + title + '\''
        + ", description='" + description + '\''
        + ", completed=" + completed
        + '}';
  }
}
