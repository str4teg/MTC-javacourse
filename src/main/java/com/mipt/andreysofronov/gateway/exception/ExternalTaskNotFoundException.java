package com.mipt.andreysofronov.gateway.exception;

/**
 * Domain exception produced when the external tasks service answers 404. The {@code detail} is
 * parsed out of the RFC 7807 {@code ProblemDetail} body returned by the upstream service.
 *
 * <p>Named distinctly from the legacy {@code com.mipt.andreysofronov.exception.TaskNotFoundException}
 * (which models the local JPA task store) to keep the two concerns unambiguous.
 */
public class ExternalTaskNotFoundException extends RuntimeException {

  private final Long taskId;

  public ExternalTaskNotFoundException(Long taskId, String detail) {
    super(detail != null ? detail : "Task not found: " + taskId);
    this.taskId = taskId;
  }

  public Long getTaskId() {
    return taskId;
  }
}
