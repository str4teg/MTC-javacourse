package com.mipt.andreysofronov.gateway.dto;

import jakarta.validation.constraints.NotBlank;

/** Payload for creating a task through the gateway. */
public record CreateTaskRequest(@NotBlank String title, boolean completed) {}
