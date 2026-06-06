package com.mipt.andreysofronov.service;

import com.mipt.andreysofronov.model.Priority;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class TaskStatisticsJdbcService {

  private final JdbcTemplate jdbcTemplate;

  public TaskStatisticsJdbcService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Map<Priority, Long> getTasksCountByPriority() {
    String sql = "select priority, count(*) as cnt from tasks group by priority";
    return jdbcTemplate.query(sql, rs -> {
      Map<Priority, Long> map = new EnumMap<>(Priority.class);
      while (rs.next()) {
        String p = rs.getString("priority");
        long cnt = rs.getLong("cnt");
        if (p != null) {
          try {
            Priority pr = Priority.valueOf(p);
            map.put(pr, cnt);
          } catch (IllegalArgumentException ignored) {
          }
        }
      }
      return map;
    });
  }

}

