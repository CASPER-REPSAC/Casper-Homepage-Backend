package com.example.newsper.migration.service;

import com.example.newsper.migration.entity.DbVersionEntity;
import com.example.newsper.migration.repository.DbVersionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationService {

    private final DbVersionRepository dbVersionRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final String ROLE_MIGRATION_VERSION = "V1.0.0_ROLE_ENUM_MIGRATION";

    @PostConstruct
    public void init() {
        try {
            runMigrations();
        } catch (Exception e) {
            log.error("마이그레이션 실행 중 오류 발생", e);
        }
    }

    @Transactional
    public void runMigrations() {
        log.info("데이터베이스 마이그레이션 시작");

        // 사용자 역할 마이그레이션 실행
        migrateUserRoles();

        log.info("데이터베이스 마이그레이션 완료");
    }

    @Transactional
    public void migrateUserRoles() {
        if (dbVersionRepository.existsByVersion(ROLE_MIGRATION_VERSION)) {
            log.info("이미 적용된 마이그레이션입니다: {}", ROLE_MIGRATION_VERSION);
            return;
        }

        log.info("사용자 역할 마이그레이션 실행: {}", ROLE_MIGRATION_VERSION);

        logCurrentRoleDistribution();

        int updatedAdmin = jdbcTemplate.update(
                "UPDATE userEntity SET role = 'admin' WHERE role IN ('ADMIN', 'Admin', 'administrator')"
        );

        int updatedAssociate = jdbcTemplate.update(
                "UPDATE userEntity SET role = 'associate' WHERE role IN ('ASSOCIATE', 'Associate')"
        );

        int updatedGraduate = jdbcTemplate.update(
                "UPDATE userEntity SET role = 'graduate' WHERE role IN ('GRADUATE', 'Graduate')"
        );

        int updatedRest = jdbcTemplate.update(
                "UPDATE userEntity SET role = 'rest' WHERE role IN ('REST', 'Rest')"
        );

        int updatedActive = jdbcTemplate.update(
                "UPDATE userEntity SET role = 'active' WHERE role IN ('ACTIVE', 'Active')"
        );

        int updatedGuest = jdbcTemplate.update(
                "UPDATE userEntity SET role = 'guest' WHERE role IN ('GUEST', 'Guest')"
        );

        int totalUpdated = updatedAdmin + updatedAssociate + updatedGraduate + updatedRest + updatedActive + updatedGuest;

        log.info("역할 마이그레이션 완료. {} 개 레코드 업데이트됨", totalUpdated);
        logCurrentRoleDistribution();

        checkNonStandardRoles();

        DbVersionEntity versionEntity = new DbVersionEntity(
                ROLE_MIGRATION_VERSION,
                LocalDateTime.now(),
                "사용자 역할을 소문자 기반 Enum으로 표준화"
        );
        dbVersionRepository.save(versionEntity);

        log.info("마이그레이션 버전 기록 완료: {}", ROLE_MIGRATION_VERSION);
    }

    private void logCurrentRoleDistribution() {
        List<Map<String, Object>> distribution = jdbcTemplate.queryForList(
                "SELECT role, COUNT(*) as count FROM userEntity GROUP BY role"
        );

        log.info("현재 역할 분포:");
        for (Map<String, Object> role : distribution) {
            log.info(" - {} : {}", role.get("role"), role.get("count"));
        }
    }

    private void checkNonStandardRoles() {
        List<Map<String, Object>> nonStandardRoles = jdbcTemplate.queryForList(
                "SELECT role, COUNT(*) as count FROM userEntity " +
                        "WHERE role NOT IN ('admin', 'associate', 'graduate', 'rest', 'active', 'guest') " +
                        "GROUP BY role"
        );

        if (!nonStandardRoles.isEmpty()) {
            log.warn("표준화되지 않은 역할이 있습니다:");
            for (Map<String, Object> role : nonStandardRoles) {
                log.warn(" - {} : {}", role.get("role"), role.get("count"));
            }
        } else {
            log.info("모든 역할이 표준화되었습니다.");
        }
    }

    public List<DbVersionEntity> getAllVersions() {
        return dbVersionRepository.findAll();
    }
}
