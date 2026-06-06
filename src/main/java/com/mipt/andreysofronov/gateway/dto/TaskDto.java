package com.mipt.andreysofronov.gateway.dto;

/** Task representation exchanged with the external service and returned by the gateway. */
public record TaskDto(Long id, String title, boolean completed) {}
