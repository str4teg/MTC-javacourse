package com.mipt.andreysofronov.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private static final String FRONTEND_ORIGIN = "http://localhost:3000";
  private static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};
  private static final String[] EXPOSED_HEADERS = {"X-Total-Count", "X-API-Version"};

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(FRONTEND_ORIGIN)
        .allowedMethods(ALLOWED_METHODS)
        .allowedHeaders("*")
        .exposedHeaders(EXPOSED_HEADERS)
        .allowCredentials(true);
  }
}
