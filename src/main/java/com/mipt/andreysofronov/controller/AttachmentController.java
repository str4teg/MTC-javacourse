package com.mipt.andreysofronov.controller;

import com.mipt.andreysofronov.dto.AttachmentDownload;
import com.mipt.andreysofronov.dto.AttachmentResponseDto;
import com.mipt.andreysofronov.dto.ErrorResponse;
import com.mipt.andreysofronov.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Attachments", description = "Файлы, прикреплённые к задачам")
public class AttachmentController {

  private static final String HEADER_TOTAL_COUNT = "X-Total-Count";

  private final AttachmentService attachmentService;

  public AttachmentController(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  @PostMapping(
      value = "/api/tasks/{taskId}/attachments",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Загрузить файл для задачи")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Файл сохранён",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AttachmentResponseDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Файл не передан или неверный запрос",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Задача не найдена",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<AttachmentResponseDto> uploadAttachment(
      @Parameter(description = "Идентификатор задачи", required = true) @PathVariable("taskId")
          Long taskId,
      @Parameter(description = "Файл (part name: file)", required = true)
          @RequestPart("file")
          MultipartFile file) {
    AttachmentResponseDto body = attachmentService.storeAttachment(taskId, file);
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }

  @GetMapping("/api/tasks/{taskId}/attachments")
  @Operation(summary = "Список вложений задачи")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Список метаданных; заголовок X-Total-Count — число вложений",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = AttachmentResponseDto.class)))),
    @ApiResponse(
        responseCode = "404",
        description = "Задача не найдена",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<List<AttachmentResponseDto>> listAttachments(
      @Parameter(description = "Идентификатор задачи", required = true) @PathVariable("taskId")
          Long taskId) {
    List<AttachmentResponseDto> body = attachmentService.listAttachmentsForTask(taskId);
    return ResponseEntity.ok()
        .header(HEADER_TOTAL_COUNT, String.valueOf(body.size()))
        .body(body);
  }

  @GetMapping("/api/attachments/{attachmentId}")
  @Operation(summary = "Скачать файл вложения")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Содержимое файла",
        content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
    @ApiResponse(
        responseCode = "404",
        description = "Вложение или файл на диске не найдены",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<Resource> downloadAttachment(
      @Parameter(description = "Идентификатор вложения", required = true)
          @PathVariable("attachmentId")
          Long attachmentId) {
    AttachmentDownload download = attachmentService.prepareDownload(attachmentId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(
        ContentDisposition.attachment()
            .filename(download.fileName(), StandardCharsets.UTF_8)
            .build());
    headers.setContentLength(download.size());

    MediaType mediaType = resolveMediaType(download.contentType());

    return ResponseEntity.ok().headers(headers).contentType(mediaType).body(download.resource());
  }

  @DeleteMapping("/api/attachments/{attachmentId}")
  @Operation(summary = "Удалить вложение")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Удалено"),
    @ApiResponse(
        responseCode = "404",
        description = "Вложение не найдено",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<Void> deleteAttachment(
      @Parameter(description = "Идентификатор вложения", required = true)
          @PathVariable("attachmentId")
          Long attachmentId) {
    attachmentService.deleteAttachment(attachmentId);
    return ResponseEntity.noContent().build();
  }

  private static MediaType resolveMediaType(String contentType) {
    if (contentType == null || contentType.isBlank()) {
      return MediaType.APPLICATION_OCTET_STREAM;
    }
    try {
      return MediaType.parseMediaType(contentType);
    } catch (RuntimeException ex) {
      return MediaType.APPLICATION_OCTET_STREAM;
    }
  }
}
