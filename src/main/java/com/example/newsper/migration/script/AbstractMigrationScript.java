package com.example.newsper.migration.script;

import com.example.newsper.migration.entity.DbVersionEntity;
import com.example.newsper.migration.repository.DbVersionRepository;

import java.time.LocalDateTime;

public abstract class AbstractMigrationScript implements MigrationScript {

    @Override
    public boolean shouldRun(DbVersionRepository dbVersionRepository) {
        return !dbVersionRepository.existsByVersion(getVersion());
    }

    protected void recordMigration(DbVersionRepository dbVersionRepository) {
        DbVersionEntity versionEntity = new DbVersionEntity(
                getVersion(),
                LocalDateTime.now(),
                getDescription()
        );
        dbVersionRepository.save(versionEntity);
    }
}
