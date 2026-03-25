package com.mipt.andreysofronov.controller;

import com.mipt.andreysofronov.dto.TaskResponseDto;
import com.mipt.andreysofronov.mapper.TaskMapper;
import com.mipt.andreysofronov.service.FavoritesService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {

  private final FavoritesService favoritesService;
  private final TaskMapper taskMapper;

  public FavoritesController(FavoritesService favoritesService, TaskMapper taskMapper) {
    this.favoritesService = favoritesService;
    this.taskMapper = taskMapper;
  }

  @GetMapping
  public ResponseEntity<List<TaskResponseDto>> getFavoriteTasks(HttpSession session) {
    List<TaskResponseDto> body =
        favoritesService.getFavoriteTasks(session).stream()
            .map(taskMapper::toResponseDto)
            .toList();
    return ResponseEntity.ok(body);
  }

  @PostMapping("/{taskId}")
  public ResponseEntity<Void> addFavorite(
      @PathVariable("taskId") Long taskId, HttpSession session) {
    favoritesService.addFavorite(session, taskId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> removeFavorite(
      @PathVariable("taskId") Long taskId, HttpSession session) {
    favoritesService.removeFavorite(session, taskId);
    return ResponseEntity.noContent().build();
  }
}
