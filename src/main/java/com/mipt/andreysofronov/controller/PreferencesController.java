package com.mipt.andreysofronov.controller;

import com.mipt.andreysofronov.dto.ViewPreferenceDto;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
public class PreferencesController {

  static final String VIEW_PREFERENCE_COOKIE = "viewPreference";
  private static final String MODE_COMPACT = "compact";
  private static final String MODE_DETAILED = "detailed";
  private static final String DEFAULT_MODE = MODE_DETAILED;

  @GetMapping("/view")
  public ResponseEntity<ViewPreferenceDto> getViewPreference(
      @CookieValue(value = VIEW_PREFERENCE_COOKIE, required = false) String modeFromCookie,
      HttpServletResponse response) {
    String effective = resolveMode(modeFromCookie);
    if (modeFromCookie == null || !isValidMode(modeFromCookie)) {
      response.addHeader(HttpHeaders.SET_COOKIE, buildViewCookie(effective).toString());
    }
    return ResponseEntity.ok(new ViewPreferenceDto(effective));
  }

  @PostMapping("/view")
  public ResponseEntity<ViewPreferenceDto> setViewPreference(
      @RequestParam("mode") String mode, HttpServletResponse response) {
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
