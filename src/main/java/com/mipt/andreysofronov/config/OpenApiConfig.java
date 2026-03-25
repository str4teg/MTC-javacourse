package com.mipt.andreysofronov.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openApi(
      @Value("${openapi.title}") String title,
      @Value("${openapi.description}") String description,
      @Value("${openapi.contact.name}") String contactName,
      @Value("${openapi.contact.email}") String contactEmail,
      @Value("${app.api.version}") String apiVersion) {
    return new OpenAPI()
        .info(
            new Info()
                .title(title)
                .description(description)
                .version(apiVersion)
                .contact(new Contact().name(contactName).email(contactEmail)));
  }
}
