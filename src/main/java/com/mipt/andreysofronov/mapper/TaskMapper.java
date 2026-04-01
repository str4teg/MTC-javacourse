package com.mipt.andreysofronov.mapper;

import com.mipt.andreysofronov.dto.TaskCreateDto;
import com.mipt.andreysofronov.dto.TaskResponseDto;
import com.mipt.andreysofronov.dto.TaskUpdateDto;
import com.mipt.andreysofronov.model.Task;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TaskMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "completed", constant = "false")
  @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(
      target = "tags",
      expression =
          "java(dto.getTags() == null ? new java.util.LinkedHashSet<>() : new java.util.LinkedHashSet<>(dto.getTags()))")
  Task toEntity(TaskCreateDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(
      target = "tags",
      expression =
          "java(dto.getTags() == null ? task.getTags() : new java.util.LinkedHashSet<>(dto.getTags()))")
  Task updateEntity(TaskUpdateDto dto, @MappingTarget Task task);

  TaskResponseDto toResponseDto(Task task);
}
