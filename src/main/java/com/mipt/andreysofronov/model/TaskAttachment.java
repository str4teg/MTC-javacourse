package com.mipt.andreysofronov.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_attachments")
public class TaskAttachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "task_id", nullable = false, insertable = false, updatable = false)
  private Long taskId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @Column(name = "file_name", nullable = false, length = 255)
  private String fileName;

  @Column(name = "stored_file_name", nullable = false, length = 255, unique = true)
  private String storedFileName;

  @Column(name = "content_type", nullable = false, length = 128)
  private String contentType;

  @Column(nullable = false)
  private long size;

  @Column(name = "uploaded_at", nullable = false)
  private LocalDateTime uploadedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getTaskId() {
    if (taskId != null) {
      return taskId;
    }
    return task != null ? task.getId() : null;
  }

  public void setTaskId(Long taskId) {
    this.taskId = taskId;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
    this.taskId = task != null ? task.getId() : null;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getStoredFileName() {
    return storedFileName;
  }

  public void setStoredFileName(String storedFileName) {
    this.storedFileName = storedFileName;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
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
