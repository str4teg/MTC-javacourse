package com.mipt.andreysofronov.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Приоритет задачи")
public enum Priority {
  LOW,
  MEDIUM,
  HIGH
}
