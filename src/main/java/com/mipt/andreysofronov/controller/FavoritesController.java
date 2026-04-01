package com.mipt.andreysofronov.controller;

import com.mipt.andreysofronov.dto.ErrorResponse;
import com.mipt.andreysofronov.dto.TaskResponseDto;
import com.mipt.andreysofronov.mapper.TaskMapper;
import com.mipt.andreysofronov.service.FavoritesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@Tag(
    name = "Favorites",
    description = "Избранные задачи (хранятся в сессии; нужны credentials / cookie JSESSIONID)")
public class FavoritesController {

  private static final String HEADER_TOTAL_COUNT = "X-Total-Count";

  private final FavoritesService favoritesService;
  private final TaskMapper taskMapper;

  public FavoritesController(FavoritesService favoritesService, TaskMapper taskMapper) {
    this.favoritesService = favoritesService;
    this.taskMapper = taskMapper;
  }

  @GetMapping
  @Operation(summary = "Список избранных задач для текущей сессии")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Список задач; заголовок X-Total-Count — число избранных",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class))))
  })
  public ResponseEntity<List<TaskResponseDto>> getFavoriteTasks(
      @Parameter(hidden = true) HttpSession session) {
    List<TaskResponseDto> body =
        favoritesService.getFavoriteTasks(session).stream()
            .map(taskMapper::toResponseDto)
            .toList();
    return ResponseEntity.ok()
        .header(HEADER_TOTAL_COUNT, String.valueOf(body.size()))
        .body(body);
  }

  @PostMapping("/{taskId}")
  @Operation(summary = "Добавить задачу в избранное")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Добавлено"),
    @ApiResponse(
        responseCode = "404",
        description = "Задача не найдена",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<Void> addFavorite(
      @Parameter(description = "Идентификатор задачи", required = true) @PathVariable("taskId")
          Long taskId,
      @Parameter(hidden = true) HttpSession session) {
    favoritesService.addFavorite(session, taskId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @DeleteMapping("/{taskId}")
  @Operation(summary = "Убрать задачу из избранного")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Удалено из избранного (если было)")
  })
  public ResponseEntity<Void> removeFavorite(
      @Parameter(description = "Идентификатор задачи", required = true) @PathVariable("taskId")
          Long taskId,
      @Parameter(hidden = true) HttpSession session) {
    favoritesService.removeFavorite(session, taskId);
    return ResponseEntity.noContent().build();
  }
}
