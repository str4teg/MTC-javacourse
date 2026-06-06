package com.mipt.andreysofronov.repository;

import com.mipt.andreysofronov.model.Priority;
import com.mipt.andreysofronov.model.Task;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findByCompletedAndPriority(Boolean completed, Priority priority);

  @Query("select t from Task t where t.dueDate between :from and :to")
  List<Task> findDueBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

  @EntityGraph(attributePaths = {"attachments"})
  @Query("select t from Task t")
  List<Task> findAllWithAttachments();

}
