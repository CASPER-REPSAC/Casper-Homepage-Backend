package com.example.newsper.migration.script;

import com.example.newsper.migration.repository.DbVersionRepository;
import org.springframework.jdbc.core.JdbcTemplate;

public interface MigrationScript {
    String getVersion();
    String getDescription();
    void execute(JdbcTemplate jdbcTemplate);
    boolean shouldRun(DbVersionRepository dbVersionRepository);
}
