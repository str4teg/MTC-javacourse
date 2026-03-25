package com.mipt.andreysofronov.controller;

import com.mipt.andreysofronov.dto.AttachmentResponseDto;
import com.mipt.andreysofronov.model.TaskAttachment;
import com.mipt.andreysofronov.service.AttachmentService;
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
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AttachmentController {

  private final AttachmentService attachmentService;

  public AttachmentController(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  @PostMapping(
      value = "/api/tasks/{taskId}/attachments",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<AttachmentResponseDto> uploadAttachment(
      @PathVariable("taskId") Long taskId, @RequestPart("file") MultipartFile file) {
    AttachmentResponseDto body = attachmentService.storeAttachment(taskId, file);
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }

  @GetMapping("/api/tasks/{taskId}/attachments")
  public List<AttachmentResponseDto> listAttachments(@PathVariable("taskId") Long taskId) {
    return attachmentService.listAttachmentsForTask(taskId);
  }

  @GetMapping("/api/attachments/{attachmentId}")
  public ResponseEntity<Resource> downloadAttachment(
      @PathVariable("attachmentId") Long attachmentId) {
    TaskAttachment attachment =
        attachmentService
            .getAttachment(attachmentId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "attachment not found"));
    Resource resource = attachmentService.loadAsResource(attachment);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(
        ContentDisposition.attachment()
            .filename(attachment.getFileName(), StandardCharsets.UTF_8)
            .build());
    headers.setContentLength(attachment.getSize());

    MediaType mediaType = resolveMediaType(attachment.getContentType());

    return ResponseEntity.ok().headers(headers).contentType(mediaType).body(resource);
  }

  @DeleteMapping("/api/attachments/{attachmentId}")
  public ResponseEntity<Void> deleteAttachment(@PathVariable("attachmentId") Long attachmentId) {
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
