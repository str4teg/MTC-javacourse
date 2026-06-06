package com.mipt.andreysofronov.exception;

import java.util.List;

public class TaskBulkCompletionException extends RuntimeException {

  private final List<Long> missingTaskIds;

  public TaskBulkCompletionException(List<Long> missingTaskIds) {
    super("Tasks not found: " + missingTaskIds);
    this.missingTaskIds = List.copyOf(missingTaskIds);
  }

  public List<Long> getMissingTaskIds() {
    return missingTaskIds;
  }
}

