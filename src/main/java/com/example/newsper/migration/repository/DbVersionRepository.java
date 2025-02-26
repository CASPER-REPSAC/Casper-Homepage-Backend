package com.example.newsper.migration.repository;

import com.example.newsper.migration.entity.DbVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbVersionRepository extends JpaRepository<DbVersionEntity, String> {
    boolean existsByVersion(String version);
}
