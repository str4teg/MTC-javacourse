package com.mipt.andreysofronov.config;

import com.mipt.andreysofronov.repository.StubTaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRepositoryConfiguration {

  @Bean
  public StubTaskRepository stubTaskRepository() {
    return new StubTaskRepository();
  }
}
