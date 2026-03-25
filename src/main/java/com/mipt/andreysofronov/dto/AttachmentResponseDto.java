package com.mipt.andreysofronov.dto;

import com.mipt.andreysofronov.model.TaskAttachment;
import java.time.LocalDateTime;

public class AttachmentResponseDto {

  private Long id;
  private String fileName;
  private long size;
  private LocalDateTime uploadedAt;

  public static AttachmentResponseDto from(TaskAttachment attachment) {
    AttachmentResponseDto dto = new AttachmentResponseDto();
    dto.setId(attachment.getId());
    dto.setFileName(attachment.getFileName());
    dto.setSize(attachment.getSize());
    dto.setUploadedAt(attachment.getUploadedAt());
    return dto;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }
}
