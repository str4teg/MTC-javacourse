package com.mipt.andreysofronov.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = DueDateNotBeforeCreationValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DueDateNotBeforeCreation {

  String message() default "dueDate must not be before the task creation date";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
