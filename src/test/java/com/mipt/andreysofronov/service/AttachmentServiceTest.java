package com.mipt.andreysofronov.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mipt.andreysofronov.dto.AttachmentResponseDto;
import com.mipt.andreysofronov.exception.TaskNotFoundException;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import com.mipt.andreysofronov.model.TaskAttachment;
import com.mipt.andreysofronov.repository.TaskAttachmentRepository;
import com.mipt.andreysofronov.repository.TaskRepository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

  @Mock private TaskAttachmentRepository attachmentRepository;
  @Mock private TaskRepository taskRepository;

  @TempDir Path tempDir;

  private AttachmentService attachmentService;
  private final AtomicLong attachmentSeq = new AtomicLong();

  @BeforeEach
  void setUp() {
    attachmentService = new AttachmentService(attachmentRepository, taskRepository, tempDir.toString());
    attachmentService.ensureUploadDirectoryExists();
    attachmentSeq.set(0);
  }

  @Test
  void storeAttachment_savesFileAndMetadata() throws Exception {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask(1L)));
    when(attachmentRepository.save(any(TaskAttachment.class)))
        .thenAnswer(
            inv -> {
              TaskAttachment a = inv.getArgument(0);
              a.setId(attachmentSeq.incrementAndGet());
              return a;
            });

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "note.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));

    AttachmentResponseDto dto = attachmentService.storeAttachment(1L, file);

    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getFileName()).isEqualTo("note.txt");
    assertThat(dto.getSize()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8).length);
    assertThat(dto.getUploadedAt()).isNotNull();

    try (var stream = Files.list(tempDir)) {
      assertThat(stream.count()).isEqualTo(1);
    }
  }

  @Test
  void storeAttachment_whenFileEmpty_throwsBadRequest() {
    MockMultipartFile empty =
        new MockMultipartFile("file", "e.txt", "text/plain", new byte[0]);

    assertThatThrownBy(() -> attachmentService.storeAttachment(1L, empty))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void storeAttachment_whenTaskMissing_throwsTaskNotFound() {
    when(taskRepository.findById(2L)).thenReturn(Optional.empty());
    MockMultipartFile file =
        new MockMultipartFile("file", "a.bin", "application/octet-stream", new byte[] {1});

    assertThatThrownBy(() -> attachmentService.storeAttachment(2L, file))
        .isInstanceOf(TaskNotFoundException.class);
  }

  @Test
  void loadAsResource_returnsFile_whenStoredOnDisk() throws Exception {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask(1L)));
    when(attachmentRepository.save(any(TaskAttachment.class)))
        .thenAnswer(
            inv -> {
              TaskAttachment a = inv.getArgument(0);
              a.setId(5L);
              return a;
            });

    MockMultipartFile file =
        new MockMultipartFile("file", "x.txt", "text/plain", "data".getBytes(StandardCharsets.UTF_8));
    AttachmentResponseDto saved = attachmentService.storeAttachment(1L, file);

    TaskAttachment meta = new TaskAttachment();
    meta.setId(saved.getId());
    meta.setStoredFileName(
        Files.list(tempDir).findFirst().map(p -> p.getFileName().toString()).orElseThrow());

    Resource resource = attachmentService.loadAsResource(meta);
    assertThat(resource.exists()).isTrue();
    assertThat(resource.getInputStream().readAllBytes())
        .isEqualTo("data".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void deleteAttachment_removesFileAndRepositoryRecord() throws Exception {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask(1L)));
    when(attachmentRepository.save(any(TaskAttachment.class)))
        .thenAnswer(
            inv -> {
              TaskAttachment a = inv.getArgument(0);
              a.setId(9L);
              return a;
            });

    MockMultipartFile file =
        new MockMultipartFile("file", "d.txt", "text/plain", "z".getBytes(StandardCharsets.UTF_8));
    attachmentService.storeAttachment(1L, file);

    Path stored =
        Files.list(tempDir).findFirst().orElseThrow();
    TaskAttachment att = new TaskAttachment();
    att.setId(9L);
    att.setStoredFileName(stored.getFileName().toString());

    when(attachmentRepository.findById(9L)).thenReturn(Optional.of(att));

    attachmentService.deleteAttachment(9L);

    verify(attachmentRepository).deleteById(9L);
    assertThat(Files.exists(stored)).isFalse();
  }

  @Test
  void listAttachmentsForTask_whenTaskMissing_throws() {
    when(taskRepository.findById(3L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> attachmentService.listAttachmentsForTask(3L))
        .isInstanceOf(TaskNotFoundException.class);
  }

  @Test
  void listAttachmentsForTask_returnsDtos() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask(1L)));
    TaskAttachment a = new TaskAttachment();
    a.setId(1L);
    a.setFileName("f.txt");
    a.setSize(10L);
    a.setUploadedAt(LocalDateTime.now());
    when(attachmentRepository.findByTaskId(1L)).thenReturn(List.of(a));

    List<AttachmentResponseDto> list = attachmentService.listAttachmentsForTask(1L);

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getFileName()).isEqualTo("f.txt");
  }

  private static Task sampleTask(Long id) {
    Task t = new Task();
    t.setId(id);
    t.setTitle("title");
    t.setDescription("d");
    t.setCompleted(false);
    t.setCreatedAt(LocalDateTime.now());
    t.setDueDate(LocalDate.now().plusDays(1));
    t.setPriority(Priority.MEDIUM);
    t.setTags(new LinkedHashSet<>(Set.of("t")));
    return t;
  }
}
