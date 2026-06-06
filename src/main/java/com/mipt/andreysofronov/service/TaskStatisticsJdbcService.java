package com.mipt.andreysofronov.service;

import com.mipt.andreysofronov.dto.TaskPriorityCountDto;
import com.mipt.andreysofronov.model.Priority;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskStatisticsJdbcService {

  private final JdbcTemplate jdbcTemplate;

  public TaskStatisticsJdbcService(JdbcTemplate jdbcTemplate) {
	this.jdbcTemplate = jdbcTemplate;
  }

  public List<TaskPriorityCountDto> getTasksCountByPriority() {
	String sql =
		"select priority, count(*) as task_count from tasks group by priority order by priority";
	return jdbcTemplate.query(
		sql,
		(rs, rowNum) -> {
		  TaskPriorityCountDto dto = new TaskPriorityCountDto();
		  dto.setPriority(Priority.valueOf(rs.getString("priority")));
		  dto.setCount(rs.getLong("task_count"));
		  return dto;
		});
  }
}

