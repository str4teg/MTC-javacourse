package com.mipt.andreysofronov.dto;

import org.springframework.core.io.Resource;

/**
 * Метаданные и {@link Resource} для ответа скачивания вложения (не сериализуется в JSON как тело).
 */
public record AttachmentDownload(
    Resource resource, String fileName, long size, String contentType) {}
