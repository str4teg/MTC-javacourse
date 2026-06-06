package com.mipt.andreysofronov.repository;

import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskJpaRepository extends JpaRepository<Task, Long> {

  List<Task> findByCompletedAndPriority(boolean completed, Priority priority);

  @Query("select t from Task t where t.dueDate >= current_date and t.dueDate <= :endDate order by t.dueDate asc")
  List<Task> findDueWithinNextSevenDays(@Param("endDate") LocalDate endDate);

  @EntityGraph(attributePaths = {"attachments", "tags"})
  @Query("select distinct t from Task t")
  List<Task> findAllWithAttachments();

  @EntityGraph(attributePaths = {"attachments", "tags"})
  @Query("select t from Task t where t.id = :id")
  Optional<Task> findWithAttachmentsById(@Param("id") Long id);
}

