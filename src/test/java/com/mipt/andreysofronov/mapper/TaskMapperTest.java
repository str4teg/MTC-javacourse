package com.mipt.andreysofronov.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mipt.andreysofronov.dto.TaskCreateDto;
import com.mipt.andreysofronov.dto.TaskResponseDto;
import com.mipt.andreysofronov.dto.TaskUpdateDto;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TaskMapperImpl.class)
@TestPropertySource(
    properties = {
      "spring.main.banner-mode=off",
      "logging.level.root=WARN"
    })
class TaskMapperTest {

  @Autowired private TaskMapper taskMapper;

  @Test
  void toEntity_mapsCreateDtoAndSetsDefaults() {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("My task");
    dto.setDescription("desc");
    dto.setDueDate(LocalDate.of(2030, 1, 15));
    dto.setPriority(Priority.HIGH);
    dto.setTags(new LinkedHashSet<>(Set.of("a", "b")));

    Task task = taskMapper.toEntity(dto);

    assertThat(task.getId()).isNull();
    assertThat(task.getTitle()).isEqualTo("My task");
    assertThat(task.getDescription()).isEqualTo("desc");
    assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2030, 1, 15));
    assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(task.isCompleted()).isFalse();
    assertThat(task.getCreatedAt()).isNotNull();
    assertThat(task.getTags()).containsExactlyInAnyOrder("a", "b");
  }

  @Test
  void toEntity_nullTags_becomesEmptySet() {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("abc");
    dto.setDescription("d");
    dto.setDueDate(LocalDate.now().plusDays(1));
    dto.setPriority(Priority.LOW);
    dto.setTags(null);

    Task task = taskMapper.toEntity(dto);

    assertThat(task.getTags()).isEmpty();
  }

  @Test
  void updateEntity_appliesNonNullFieldsAndIgnoresNulls() {
    Task task = new Task();
    task.setId(5L);
    task.setTitle("Old");
    task.setDescription("Old desc");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.of(2020, 1, 1, 12, 0));
    task.setDueDate(LocalDate.of(2025, 6, 1));
    task.setPriority(Priority.MEDIUM);
    task.setTags(new LinkedHashSet<>(Set.of("x")));

    TaskUpdateDto dto = new TaskUpdateDto();
    dto.setTitle("New title");
    dto.setCompleted(true);

    taskMapper.updateEntity(dto, task);

    assertThat(task.getId()).isEqualTo(5L);
    assertThat(task.getTitle()).isEqualTo("New title");
    assertThat(task.getDescription()).isEqualTo("Old desc");
    assertThat(task.isCompleted()).isTrue();
    assertThat(task.getCreatedAt()).isEqualTo(LocalDateTime.of(2020, 1, 1, 12, 0));
    assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2025, 6, 1));
    assertThat(task.getPriority()).isEqualTo(Priority.MEDIUM);
    assertThat(task.getTags()).containsExactly("x");
  }

  @Test
  void updateEntity_replacesTagsWhenProvided() {
    Task task = new Task();
    task.setTitle("t");
    task.setCreatedAt(LocalDateTime.now());
    task.setDueDate(LocalDate.now().plusDays(1));
    task.setPriority(Priority.LOW);
    task.setTags(new LinkedHashSet<>(Set.of("old")));

    TaskUpdateDto dto = new TaskUpdateDto();
    dto.setTags(new LinkedHashSet<>(Set.of("n1", "n2")));

    taskMapper.updateEntity(dto, task);

    assertThat(task.getTags()).containsExactlyInAnyOrder("n1", "n2");
  }

  @Test
  void toResponseDto_mapsAllFields() {
    Task task = new Task();
    task.setId(10L);
    task.setTitle("Response");
    task.setDescription("D");
    task.setCompleted(true);
    task.setCreatedAt(LocalDateTime.of(2024, 3, 20, 10, 30));
    task.setDueDate(LocalDate.of(2024, 4, 1));
    task.setPriority(Priority.LOW);
    task.setTags(new LinkedHashSet<>(Set.of("t1")));

    TaskResponseDto dto = taskMapper.toResponseDto(task);

    assertThat(dto.getId()).isEqualTo(10L);
    assertThat(dto.getTitle()).isEqualTo("Response");
    assertThat(dto.getDescription()).isEqualTo("D");
    assertThat(dto.isCompleted()).isTrue();
    assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 3, 20, 10, 30));
    assertThat(dto.getDueDate()).isEqualTo(LocalDate.of(2024, 4, 1));
    assertThat(dto.getPriority()).isEqualTo(Priority.LOW);
    assertThat(dto.getTags()).containsExactly("t1");
  }
}
