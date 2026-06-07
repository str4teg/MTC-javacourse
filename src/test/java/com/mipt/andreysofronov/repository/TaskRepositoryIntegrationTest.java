package com.mipt.andreysofronov.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mipt.andreysofronov.config.JpaAuditingConfig;
import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Часть 3: интеграционное тестирование слоя репозиториев на РЕАЛЬНОМ PostgreSQL в Docker
 * (Testcontainers), а не на H2.
 *
 * <p>{@link DataJpaTest} оборачивает каждый тест в транзакцию с откатом, поэтому тесты изолированы и
 * не зависят от порядка выполнения. {@link AutoConfigureTestDatabase.Replace#NONE} отключает
 * подмену источника данных встроенной БД — используется датасорс контейнера, переданный через
 * {@link DynamicPropertySource}.
 */
@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class TaskRepositoryIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("todo")
          .withUsername("todo")
          .withPassword("todo");

  @DynamicPropertySource
  static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add(
        "spring.jpa.properties.hibernate.dialect",
        () -> "org.hibernate.dialect.PostgreSQLDialect");
    registry.add("spring.flyway.enabled", () -> "false");
  }

  @Autowired private TaskJpaRepository taskRepository;

  @Test
  void usesRealPostgresContainer() {
    assertThat(POSTGRES.isRunning()).isTrue();
    assertThat(POSTGRES.getJdbcUrl()).startsWith("jdbc:postgresql://");
  }

  @Test
  void findDueWithinNextSevenDays_returnsOnlyTasksInsideWindow() {
    // given: задачи со сроком внутри и за пределами семидневного окна
    taskRepository.saveAll(
        List.of(
            task("soon", LocalDate.now().plusDays(3)),
            task("boundary", LocalDate.now().plusDays(7)),
            task("late", LocalDate.now().plusDays(8))));

    // when: выполняем кастомный @Query из ДЗ №3 (поиск задач по дате)
    List<Task> result = taskRepository.findDueWithinNextSevenDays(LocalDate.now().plusDays(7));

    // then: вернулись только задачи в пределах окна, упорядоченные по сроку
    assertThat(result).extracting(Task::getTitle).containsExactly("soon", "boundary");
  }

  private static Task task(String title, LocalDate dueDate) {
    Task task = new Task();
    task.setTitle(title);
    task.setDescription(title + "-desc");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now());
    task.setUpdatedAt(LocalDateTime.now());
    task.setDueDate(dueDate);
    task.setPriority(Priority.MEDIUM);
    task.setTags(new LinkedHashSet<>(Set.of("tag")));
    return task;
  }
}
