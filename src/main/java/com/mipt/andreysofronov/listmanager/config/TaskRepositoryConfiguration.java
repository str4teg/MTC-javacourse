package com.mipt.andreysofronov.listmanager.config;

import com.mipt.andreysofronov.listmanager.repository.StubTaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRepositoryConfiguration {

  @Bean
  public StubTaskRepository stubTaskRepository() {
    return new StubTaskRepository();
  }
}
