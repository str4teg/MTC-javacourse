package com.mipt.andreysofronov.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Security scenarios 1–5 from the spec: login, JSON 401 without a token, role-based access to
 * {@code /profile} and authority-based access to {@code /docs}. Uses real signed tokens so the JWT
 * filter is exercised end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private String login(String username, String password) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(body).get("accessToken").asText();
  }

  @Test
  void login_returnsJwtToken() throws Exception {
    String token = login("user", "password");
    assertThat(token).isNotBlank();
    // Header.Payload.Signature
    assertThat(token.split("\\.")).hasSize(3);
  }

  @Test
  void login_withWrongPassword_returns401() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(401));
  }

  @Test
  void profile_withoutToken_returns401Json() throws Exception {
    mockMvc
        .perform(get("/api/v1/profile"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("Unauthorized"));
  }

  @Test
  void profile_withUserToken_returns200() throws Exception {
    String token = login("user", "password");
    mockMvc
        .perform(get("/api/v1/profile").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("user"))
        .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));
  }

  @Test
  void docs_withUserToken_returns403Json() throws Exception {
    String token = login("user", "password");
    mockMvc
        .perform(get("/api/v1/docs").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void docs_withReaderToken_returns200() throws Exception {
    String token = login("reader", "password");
    mockMvc
        .perform(get("/api/v1/docs").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reader").value("reader"));
  }

  @Test
  void invalidBearerToken_isRejectedWith401() throws Exception {
    mockMvc
        .perform(get("/api/v1/profile").header("Authorization", "Bearer not.a.jwt"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void traceIdHeaderIsEchoedOnResponses() throws Exception {
    mockMvc
        .perform(get("/api/v1/profile").header("X-Trace-Id", "trace-abc-123"))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(result -> assertThat(result.getResponse().getHeader("X-Trace-Id"))
            .isEqualTo("trace-abc-123"));
  }
}
