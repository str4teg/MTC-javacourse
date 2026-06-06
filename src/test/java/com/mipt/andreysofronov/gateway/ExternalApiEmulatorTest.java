package com.mipt.andreysofronov.gateway;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Verifies the external API emulator: 201+Location, 204, 404+ProblemDetail and unstable modes. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExternalApiEmulatorTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void create_returns201WithLocationAndBody() throws Exception {
    mockMvc
        .perform(
            post("/external/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"emulated\",\"completed\":true}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.title").value("emulated"))
        .andExpect(jsonPath("$.completed").value(true));
  }

  @Test
  void getMissing_returns404ProblemDetail() throws Exception {
    mockMvc
        .perform(get("/external/v1/tasks/424242"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("Task 424242 was not found"))
        .andExpect(jsonPath("$.taskId").value(424242));
  }

  @Test
  void deleteMissing_returns404() throws Exception {
    mockMvc.perform(delete("/external/v1/tasks/999999")).andExpect(status().isNotFound());
  }

  @Test
  void createThenDelete_returns204() throws Exception {
    String body =
        mockMvc
            .perform(
                post("/external/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"to-delete\",\"completed\":false}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(body).get("id").asLong();

    mockMvc.perform(delete("/external/v1/tasks/" + id)).andExpect(status().isNoContent());
  }

  @Test
  void unstable500_returnsProblemDetail() throws Exception {
    mockMvc
        .perform(get("/external/v1/unstable").param("mode", "500"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("Simulated upstream failure"));
  }

  @Test
  void unstable429_returnsRetryAfter() throws Exception {
    mockMvc
        .perform(get("/external/v1/unstable").param("mode", "429"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().string("Retry-After", "2"));
  }

  @Test
  void unstableHtml_returns502TextHtml() throws Exception {
    mockMvc
        .perform(get("/external/v1/unstable").param("mode", "html"))
        .andExpect(status().isBadGateway())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
  }
}
