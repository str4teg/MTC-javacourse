package com.mipt.andreysofronov.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mipt.andreysofronov.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PreferencesController.class)
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
class PreferencesControllerWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void getViewPreference_withoutCookie_returns200AndDetailedDefault() throws Exception {
    mockMvc
        .perform(get("/api/preferences/view").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mode").value("detailed"));
  }

  @Test
  void setViewPreference_compact_returns200() throws Exception {
    mockMvc
        .perform(post("/api/preferences/view").param("mode", "compact"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mode").value("compact"));
  }

  @Test
  void setViewPreference_invalidMode_returns400() throws Exception {
    mockMvc
        .perform(post("/api/preferences/view").param("mode", "wide"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }
}
