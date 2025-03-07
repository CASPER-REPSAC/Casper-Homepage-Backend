package com.example.newsper.migration.service;

import com.example.newsper.migration.repository.DbVersionRepository;
import com.example.newsper.migration.script.MigrationScript;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MigrationService {

    private final Logger log = LoggerFactory.getLogger(MigrationService.class);
    private final List<MigrationScript> migrationScripts;
    private final JdbcTemplate jdbcTemplate;
    private final DbVersionRepository dbVersionRepository;

    @Autowired
    public MigrationService(
            List<MigrationScript> migrationScripts,
            JdbcTemplate jdbcTemplate,
            DbVersionRepository dbVersionRepository) {
        this.migrationScripts = migrationScripts;
        this.jdbcTemplate = jdbcTemplate;
        this.dbVersionRepository = dbVersionRepository;

        log.info("마이그레이션 서비스 초기화. 등록된 스크립트: {} 개", migrationScripts.size());

        sortScriptsByVersion();
    }

    /**
     * 마이그레이션 스크립트를 시맨틱 버전 순서대로 정렬합니다.
     */
    private void sortScriptsByVersion() {
        migrationScripts.sort((a, b) -> {
            Comparable<?> versionA = parseVersion(a);
            Comparable<?> versionB = parseVersion(b);

            // 타입에 따른 비교 처리
            if (versionA instanceof SemanticVersion && versionB instanceof SemanticVersion) {
                return ((SemanticVersion) versionA).compareTo((SemanticVersion) versionB);
            } else if (versionA instanceof Integer && versionB instanceof Integer) {
                return ((Integer) versionA).compareTo((Integer) versionB);
            } else {
                return versionA.toString().compareTo(versionB.toString());
            }
        });
        log.debug("정렬된 마이그레이션 스크립트:");
        for (MigrationScript script : migrationScripts) {
            log.debug(" - {} ({})", script.getVersion(), script.getDescription());
        }
    }

    /**
     * 시맨틱 버전 문자열을 비교 가능한 객체로 파싱합니다.
     */
    private Comparable<?> parseVersion(MigrationScript script) {
        String version = script.getVersion();

        try {
            // 단순 시맨틱 버전 파싱 (x.y.z 형식)
            String[] parts = version.split("\\.");
            if (parts.length >= 3) {
                return new SemanticVersion(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2])
                );
            }
            // 숫자만 있는 경우
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            // 숫자로 파싱할 수 없는 경우 문자열 그대로 사용
            return version;
        }
    }

    /**
         * 시맨틱 버전을 표현하는 내부 클래스
         */
        private record SemanticVersion(int major, int minor, int patch) implements Comparable<SemanticVersion> {

        @Override
            public int compareTo(SemanticVersion other) {
                if (major != other.major) {
                    return Integer.compare(major, other.major);
                }
                if (minor != other.minor) {
                    return Integer.compare(minor, other.minor);
                }
                return Integer.compare(patch, other.patch);
            }
        }

    /**
     * 애플리케이션 시작 시 마이그레이션을 자동으로 실행합니다.
     */
    @PostConstruct
    public void init() {
        try {
            log.info("애플리케이션 시작 시 마이그레이션 자동 실행");
            runMigrations();
        } catch (Exception e) {
            log.error("마이그레이션 실행 중 오류 발생", e);
            // 애플리케이션 시작을 중단할지 결정
            // throw new RuntimeException("마이그레이션 실패로 애플리케이션 시작 중단", e);
        }
    }

    /**
     * 모든 마이그레이션 스크립트를 실행합니다.
     */
    public void runMigrations() {
        log.info("데이터베이스 마이그레이션 시작");

        int executed = 0;
        int skipped = 0;

        for (MigrationScript script : migrationScripts) {
            try {
                if (script.shouldRun(dbVersionRepository)) {
                    log.info("마이그레이션 '{}' ({}) 실행 중...",
                            script.getVersion(), script.getDescription());

                    long startTime = System.currentTimeMillis();
                    script.execute(jdbcTemplate);
                    long duration = System.currentTimeMillis() - startTime;

                    log.info("마이그레이션 '{}' 완료 ({}ms 소요)",
                            script.getVersion(), duration);
                    executed++;
                } else {
                    log.debug("마이그레이션 '{}' 건너뜀 (이미 적용됨)", script.getVersion());
                    skipped++;
                }
            } catch (Exception e) {
                log.error("마이그레이션 '{}' ({}) 실행 중 오류 발생: {}",
                        script.getVersion(), script.getDescription(), e.getMessage(), e);
                throw new MigrationException("마이그레이션 실행 실패: " + script.getVersion(), e);
            }
        }

        log.info("데이터베이스 마이그레이션 완료. 실행: {} 개, 건너뜀: {} 개", executed, skipped);
    }

    /**
     * 마이그레이션 관련 예외
     */
    public static class MigrationException extends RuntimeException {
        public MigrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
