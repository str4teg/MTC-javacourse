package com.mipt.andreysofronov.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mipt.andreysofronov.dto.AttachmentDownload;
import com.mipt.andreysofronov.dto.AttachmentResponseDto;
import com.mipt.andreysofronov.exception.GlobalExceptionHandler;
import com.mipt.andreysofronov.exception.TaskNotFoundException;
import com.mipt.andreysofronov.service.AttachmentService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(controllers = AttachmentController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(
    properties = {
      "spring.profiles.active=test",
      "app.name=test-app",
      "app.version=0-test",
      "app.api.version=2.0.0",
      "openapi.title=T",
      "openapi.description=D",
      "openapi.contact.name=N",
      "openapi.contact.email=n@e.com"
    })
class AttachmentControllerWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AttachmentService attachmentService;

  @Test
  void uploadAttachment_whenOk_returns201() throws Exception {
    AttachmentResponseDto dto = new AttachmentResponseDto();
    dto.setId(1L);
    dto.setFileName("a.txt");
    dto.setSize(3L);
    dto.setUploadedAt(LocalDateTime.now());
    when(attachmentService.storeAttachment(eq(5L), any())).thenReturn(dto);

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "a.txt", "text/plain", "abc".getBytes(StandardCharsets.UTF_8));

    mockMvc
        .perform(multipart("/api/tasks/5/attachments").file(file))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.fileName").value("a.txt"));
  }

  @Test
  void uploadAttachment_whenTaskNotFound_returns404() throws Exception {
    when(attachmentService.storeAttachment(eq(9L), any()))
        .thenThrow(new TaskNotFoundException(9L));

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "a.txt", "text/plain", "x".getBytes(StandardCharsets.UTF_8));

    mockMvc
        .perform(multipart("/api/tasks/9/attachments").file(file))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void uploadAttachment_whenServiceRejectsEmptyFile_returns400() throws Exception {
    doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required"))
        .when(attachmentService)
        .storeAttachment(eq(1L), any());

    MockMultipartFile empty =
        new MockMultipartFile("file", "e.txt", "text/plain", new byte[0]);

    mockMvc
        .perform(multipart("/api/tasks/1/attachments").file(empty))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void listAttachments_whenOk_returns200() throws Exception {
    AttachmentResponseDto dto = new AttachmentResponseDto();
    dto.setId(2L);
    dto.setFileName("f.bin");
    dto.setSize(10L);
    dto.setUploadedAt(LocalDateTime.now());
    when(attachmentService.listAttachmentsForTask(3L)).thenReturn(List.of(dto));

    mockMvc
        .perform(get("/api/tasks/3/attachments"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Total-Count", "1"))
        .andExpect(jsonPath("$[0].id").value(2))
        .andExpect(jsonPath("$[0].fileName").value("f.bin"));
  }

  @Test
  void listAttachments_whenTaskNotFound_returns404() throws Exception {
    when(attachmentService.listAttachmentsForTask(99L)).thenThrow(new TaskNotFoundException(99L));

    mockMvc
        .perform(get("/api/tasks/99/attachments"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void downloadAttachment_whenOk_returnsFile() throws Exception {
    Resource resource = new ByteArrayResource("data".getBytes(StandardCharsets.UTF_8));
    AttachmentDownload download =
        new AttachmentDownload(resource, "doc.pdf", 4L, "application/pdf");
    when(attachmentService.prepareDownload(7L)).thenReturn(download);

    mockMvc
        .perform(get("/api/attachments/7"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("doc.pdf")))
        .andExpect(content().bytes("data".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void downloadAttachment_whenMissing_returns404() throws Exception {
    when(attachmentService.prepareDownload(8L))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "attachment not found"));

    mockMvc
        .perform(get("/api/attachments/8"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void deleteAttachment_whenOk_returns204() throws Exception {
    mockMvc.perform(delete("/api/attachments/3")).andExpect(status().isNoContent());
  }

  @Test
  void deleteAttachment_whenNotFound_returns404() throws Exception {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "attachment not found"))
        .when(attachmentService)
        .deleteAttachment(4L);

    mockMvc
        .perform(delete("/api/attachments/4"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }
}
