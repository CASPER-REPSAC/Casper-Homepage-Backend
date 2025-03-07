package com.example.newsper.migration.script;

import com.example.newsper.migration.repository.DbVersionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EnumMigrationScript extends AbstractMigrationScript {

    private static final String VERSION = "1.0.0";
    private final DbVersionRepository dbVersionRepository;

    public EnumMigrationScript(DbVersionRepository dbVersionRepository) {
        this.dbVersionRepository = dbVersionRepository;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getDescription() {
        return "사용자 역할을 소문자 기반 Enum으로 표준화";
    }

    @Override
    @Transactional
    public void execute(JdbcTemplate jdbcTemplate) {
        if (!shouldRun(dbVersionRepository)) {
            log.info("이미 적용된 마이그레이션입니다: {}", getVersion());
            return;
        }

        log.info("사용자 역할 마이그레이션 실행: {}", getVersion());
        logCurrentRoleDistribution(jdbcTemplate);

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
        log.info("사용자 역할 마이그레이션 완료: {} 건 업데이트", totalUpdated);

        // 표준화되지 않은 역할이 있는지 확인
        checkNonStandardRoles(jdbcTemplate);

        // 마이그레이션 완료 기록
        recordMigration(dbVersionRepository);
        log.info("마이그레이션 버전 기록 완료: {}", getVersion());
    }

    private void logCurrentRoleDistribution(JdbcTemplate jdbcTemplate) {
        log.info("현재 역할 분포 조회 중...");

        // 모든 역할 종류 조회
        List<Map<String, Object>> roleCounts = jdbcTemplate.queryForList(
                "SELECT role, COUNT(*) as count FROM userEntity GROUP BY role"
        );

        log.info("현재 역할 분포:");
        for (Map<String, Object> roleCount : roleCounts) {
            log.info(" - {}: {} 명", roleCount.get("role"), roleCount.get("count"));
        }

        // 전체 사용자 수
        Integer totalUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM userEntity", Integer.class
        );
        log.info("전체 사용자 수: {} 명", totalUsers);
    }

    private void checkNonStandardRoles(JdbcTemplate jdbcTemplate) {
        log.info("표준화되지 않은 역할 확인 중...");

        // 표준 역할 목록 (소문자)
        String[] standardRoles = {"admin", "associate", "graduate", "rest", "active", "guest"};

        StringBuilder query = new StringBuilder();
        query.append("SELECT role, COUNT(*) as count FROM userEntity WHERE role NOT IN (");

        for (int i = 0; i < standardRoles.length; i++) {
            query.append("'").append(standardRoles[i]).append("'");
            if (i < standardRoles.length - 1) {
                query.append(", ");
            }
        }
        query.append(") GROUP BY role");

        List<Map<String, Object>> nonStandardRoles = jdbcTemplate.queryForList(query.toString());

        if (nonStandardRoles.isEmpty()) {
            log.info("표준화되지 않은 역할이 없습니다.");
        } else {
            log.warn("표준화되지 않은 역할이 발견되었습니다:");
            for (Map<String, Object> roleCount : nonStandardRoles) {
                log.warn(" - {}: {} 명", roleCount.get("role"), roleCount.get("count"));
            }
            log.warn("이러한 역할은 수동으로 검토하고 처리해야 합니다.");
        }
    }
}
