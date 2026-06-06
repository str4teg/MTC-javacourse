package com.mipt.andreysofronov.model;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(length = 500)
  private String description;

  @Column(nullable = false)
  private boolean completed;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Priority priority;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "task_tags", joinColumns = @JoinColumn(name = "task_id"))
  @Column(name = "tag", nullable = false, length = 64)
  private Set<String> tags = new LinkedHashSet<>();

  @OneToMany(
      mappedBy = "task",
      fetch = FetchType.LAZY,
      cascade = CascadeType.REMOVE,
      orphanRemoval = true)
  @OrderBy("uploadedAt ASC")
  private List<TaskAttachment> attachments = new ArrayList<>();

  public Long getId() {
	return id;
  }

  public void setId(Long id) {
	this.id = id;
  }

  public String getTitle() {
	return title;
  }

  public void setTitle(String title) {
	this.title = title;
  }

  public String getDescription() {
	return description;
  }

  public void setDescription(String description) {
	this.description = description;
  }

  public boolean isCompleted() {
	return completed;
  }

  public void setCompleted(boolean completed) {
	this.completed = completed;
  }

  public LocalDateTime getCreatedAt() {
	return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
	this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
	return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
	this.updatedAt = updatedAt;
  }

  public LocalDate getDueDate() {
	return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
	this.dueDate = dueDate;
  }

  public Priority getPriority() {
	return priority;
  }

  public void setPriority(Priority priority) {
	this.priority = priority;
  }

  public Set<String> getTags() {
	return tags;
  }

  public void setTags(Set<String> tags) {
	this.tags = tags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tags);
  }

  public List<TaskAttachment> getAttachments() {
	return attachments;
  }

  public void setAttachments(List<TaskAttachment> attachments) {
	this.attachments = attachments == null ? new ArrayList<>() : new ArrayList<>(attachments);
  }

  public void addAttachment(TaskAttachment attachment) {
	if (attachment == null) {
	  return;
	}
	attachments.add(attachment);
	attachment.setTask(this);
  }

  public void removeAttachment(TaskAttachment attachment) {
	if (attachment == null) {
	  return;
	}
	attachments.remove(attachment);
	attachment.setTask(null);
  }
}



