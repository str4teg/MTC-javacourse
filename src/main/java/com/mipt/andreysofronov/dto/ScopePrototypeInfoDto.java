package com.mipt.andreysofronov.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ демо prototype scope")
public record ScopePrototypeInfoDto(
    @Schema(description = "Идентификатор первого экземпляра генератора") String firstGeneratorInstanceId,
    @Schema(description = "Идентификатор второго экземпляра генератора") String secondGeneratorInstanceId,
    @Schema(description = "true, если ссылки указывают на один объект") boolean sameBeanInstance,
    @Schema(description = "Сгенерированный id задачи (первый бин)") String taskIdFromFirst,
    @Schema(description = "Сгенерированный id задачи (второй бин)") String taskIdFromSecond) {}
