package com.mipt.andreysofronov.dto;

import com.mipt.andreysofronov.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество задач по приоритетам")
public class TaskPriorityCountDto {

  @Schema(description = "Приоритет задачи")
  private Priority priority;

  @Schema(description = "Количество задач")
  private long count;

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }
}

