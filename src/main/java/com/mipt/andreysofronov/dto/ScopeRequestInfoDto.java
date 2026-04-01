package com.mipt.andreysofronov.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Ответ демо request scope")
public record ScopeRequestInfoDto(
    @Schema(description = "Уникальный идентификатор запроса") String requestId,
    @Schema(description = "Момент начала обработки") Instant processingStartedAt,
    @Schema(description = "Подсказка по поведению") String note) {}
