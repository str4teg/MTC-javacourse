package com.mipt.andreysofronov.controller;

import com.mipt.andreysofronov.dto.ErrorResponse;
import com.mipt.andreysofronov.dto.ViewPreferenceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/preferences")
@Tag(name = "Preferences", description = "Настройки отображения (cookie viewPreference)")
public class PreferencesController {

  static final String VIEW_PREFERENCE_COOKIE = "viewPreference";
  private static final String MODE_COMPACT = "compact";
  private static final String MODE_DETAILED = "detailed";
  private static final String DEFAULT_MODE = MODE_DETAILED;

  @GetMapping("/view")
  @Operation(summary = "Текущий режим просмотра (читает cookie; при отсутствии выставляет по умолчанию)")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Текущий режим",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ViewPreferenceDto.class)))
  })
  public ResponseEntity<ViewPreferenceDto> getViewPreference(
      @Parameter(description = "Значение cookie viewPreference", hidden = false)
          @CookieValue(value = VIEW_PREFERENCE_COOKIE, required = false)
          String modeFromCookie,
      @Parameter(hidden = true) HttpServletResponse response) {
    String effective = resolveMode(modeFromCookie);
    if (modeFromCookie == null || !isValidMode(modeFromCookie)) {
      response.addHeader(HttpHeaders.SET_COOKIE, buildViewCookie(effective).toString());
    }
    return ResponseEntity.ok(new ViewPreferenceDto(effective));
  }

  @PostMapping("/view")
  @Operation(summary = "Установить режим просмотра (обновляет cookie)")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Режим сохранён",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ViewPreferenceDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Недопустимое значение mode",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<ViewPreferenceDto> setViewPreference(
      @Parameter(
              description = "Режим: compact или detailed",
              required = true,
              example = "detailed")
          @RequestParam("mode")
          String mode,
      @Parameter(hidden = true) HttpServletResponse response) {
    if (!isValidMode(mode)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "mode must be " + MODE_COMPACT + " or " + MODE_DETAILED);
    }
    String normalized = mode.toLowerCase();
    response.addHeader(HttpHeaders.SET_COOKIE, buildViewCookie(normalized).toString());
    return ResponseEntity.ok(new ViewPreferenceDto(normalized));
  }

  private static String resolveMode(String cookieValue) {
    if (cookieValue == null) {
      return DEFAULT_MODE;
    }
    return isValidMode(cookieValue) ? cookieValue.toLowerCase() : DEFAULT_MODE;
  }

  private static boolean isValidMode(String mode) {
    if (mode == null) {
      return false;
    }
    String m = mode.toLowerCase();
    return MODE_COMPACT.equals(m) || MODE_DETAILED.equals(m);
  }

  private static ResponseCookie buildViewCookie(String mode) {
    return ResponseCookie.from(VIEW_PREFERENCE_COOKIE, mode)
        .path("/")
        .maxAge(Duration.ofDays(365))
        .httpOnly(true)
        .sameSite("Lax")
        .build();
  }
}
