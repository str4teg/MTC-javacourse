package com.mipt.andreysofronov.validation;

import com.mipt.andreysofronov.dto.TaskUpdateDto;
import com.mipt.andreysofronov.repository.TaskRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class DueDateNotBeforeCreationValidator
    implements ConstraintValidator<DueDateNotBeforeCreation, TaskUpdateDto> {

  private static final Pattern TASK_ID_IN_URI =
      Pattern.compile("/api/tasks/(\\d+)(?:/)?$");

  private final TaskRepository taskRepository;

  public DueDateNotBeforeCreationValidator(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @Override
  public boolean isValid(TaskUpdateDto dto, ConstraintValidatorContext context) {
    if (dto == null || dto.getDueDate() == null) {
      return true;
    }
    Long id = resolveTaskIdFromRequest();
    if (id == null) {
      return true;
    }
    return taskRepository
        .findById(id)
        .map(
            task -> {
              var createdDay = task.getCreatedAt().toLocalDate();
              return !dto.getDueDate().isBefore(createdDay);
            })
        .orElse(true);
  }

  private static Long resolveTaskIdFromRequest() {
    RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
    if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
      return null;
    }
    HttpServletRequest request = servletAttrs.getRequest();
    String uri = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
      uri = uri.substring(contextPath.length());
    }
    Matcher m = TASK_ID_IN_URI.matcher(uri);
    if (!m.find()) {
      return null;
    }
    return Long.parseLong(m.group(1));
  }
}
