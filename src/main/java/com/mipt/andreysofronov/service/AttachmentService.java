package com.mipt.andreysofronov.service;

import com.mipt.andreysofronov.dto.AttachmentDownload;
import com.mipt.andreysofronov.dto.AttachmentResponseDto;
import com.mipt.andreysofronov.exception.TaskNotFoundException;
import com.mipt.andreysofronov.model.TaskAttachment;
import com.mipt.andreysofronov.repository.TaskAttachmentRepository;
import com.mipt.andreysofronov.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttachmentService {

  private final TaskAttachmentRepository attachmentRepository;
  private final TaskRepository taskRepository;
  private final Path uploadRoot;

  public AttachmentService(
      TaskAttachmentRepository attachmentRepository,
      TaskRepository taskRepository,
      @Value("${app.uploads.directory:uploads}") String uploadDirectory) {
    this.attachmentRepository = attachmentRepository;
    this.taskRepository = taskRepository;
    this.uploadRoot = Path.of(uploadDirectory).toAbsolutePath().normalize();
  }

  @PostConstruct
  public void ensureUploadDirectoryExists() {
    try {
      Files.createDirectories(uploadRoot);
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot create upload directory: " + uploadRoot, e);
    }
  }

  public AttachmentResponseDto storeAttachment(Long taskId, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
    }
    taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));

    String originalName = safeOriginalFileName(file.getOriginalFilename());
    String storedName = buildStoredFileName(originalName);
    Path target = uploadRoot.resolve(storedName);

    try (InputStream in = file.getInputStream()) {
      Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to store file", e);
    }

    long size;
    try {
      size = Files.size(target);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read stored file size", e);
    }

    String contentType = file.getContentType();
    if (contentType == null || contentType.isBlank()) {
      contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    TaskAttachment attachment = new TaskAttachment();
    // привязываем к сущности Task
    com.mipt.andreysofronov.model.Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    attachment.setTask(task);
    attachment.setFileName(originalName);
    attachment.setStoredFileName(storedName);
    attachment.setContentType(contentType);
    attachment.setSize(size);
    attachment.setUploadedAt(LocalDateTime.now());

    TaskAttachment saved = attachmentRepository.save(attachment);
    return AttachmentResponseDto.from(saved);
  }

  public AttachmentDownload prepareDownload(Long attachmentId) {
    TaskAttachment attachment =
        attachmentRepository
            .findById(attachmentId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "attachment not found"));
    Resource resource = loadAsResource(attachment);
    return new AttachmentDownload(
        resource, attachment.getFileName(), attachment.getSize(), attachment.getContentType());
  }

  public Resource loadAsResource(TaskAttachment attachment) {
    Path path = uploadRoot.resolve(attachment.getStoredFileName());
    if (!Files.isRegularFile(path)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "stored file not found");
    }
    return new FileSystemResource(path);
  }

  public void deleteAttachment(Long attachmentId) {
    TaskAttachment attachment =
        attachmentRepository
            .findById(attachmentId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "attachment not found"));
    Path path = uploadRoot.resolve(attachment.getStoredFileName());
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to delete file", e);
    }
    attachmentRepository.deleteById(attachmentId);
  }

  public List<AttachmentResponseDto> listAttachmentsForTask(Long taskId) {
    taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    return attachmentRepository.findByTaskId(taskId).stream()
        .map(AttachmentResponseDto::from)
        .toList();
  }

  private static String safeOriginalFileName(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      return "file";
    }
    String name = Path.of(originalFilename).getFileName().toString();
    if (name.isBlank()) {
      return "file";
    }
    return name;
  }

  private static String buildStoredFileName(String originalName) {
    String ext = "";
    int dot = originalName.lastIndexOf('.');
    if (dot > 0 && dot < originalName.length() - 1) {
      ext = originalName.substring(dot);
      if (ext.length() > 32) {
        ext = "";
      }
    }
    return UUID.randomUUID() + ext;
  }
}
