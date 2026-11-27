package com._s3k.runsync.domain.run.repository;

import com._s3k.runsync.entity.RunRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RunRecordRepository extends JpaRepository<RunRecord, Long> {

    Optional<RunRecord> findByRunningSessionId(Long sessionId);

    @Query("SELECT COALESCE(SUM(r.distance), 0) AS totalDistance, COUNT(r) AS totalRunCount, COALESCE(SUM(r.durationSeconds), 0) AS totalDurationSeconds FROM RunRecord r WHERE r.user.id = :userId AND r.startTime >= :start AND r.startTime < :end")
    MonthlyStatsProjection findMonthlyStats(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<RunRecord> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    List<RunRecord> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long cursor, Pageable pageable);

    // 거리와 시간이 모두 0보다 큰 기록만 최근 순으로. 시작 직후 종료한 기록은 페이스를 계산할 수 없어 제외한다
    @Query("SELECT r FROM RunRecord r WHERE r.user.id = :userId AND r.distance > 0 AND r.durationSeconds > 0 ORDER BY r.id DESC")
    List<RunRecord> findRecentWithDistanceAndDuration(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.distance), 0) FROM RunRecord r WHERE r.user.id = :userId AND r.startTime >= :start AND r.startTime < :end")
    BigDecimal sumDistanceBetween(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT r FROM RunRecord r LEFT JOIN FETCH r.paths WHERE r.id = :recordId")
    Optional<RunRecord> findByIdWithPaths(@Param("recordId") Long recordId);

    @Query("SELECT DISTINCT r FROM RunRecord r LEFT JOIN FETCH r.paths " +
            "JOIN r.runningSession s WHERE s.artRunSessionId = :artRunSessionId")
    List<RunRecord> findByArtRunSessionIdWithPaths(@Param("artRunSessionId") Long artRunSessionId);

    @Query("""
            SELECT r FROM RunRecord r
            WHERE r.user.id IN :userIds
            AND r.startTime = (SELECT MAX(r2.startTime) FROM RunRecord r2 WHERE r2.user.id = r.user.id)
            """)
    List<RunRecord> findLatestByUserIds(@Param("userIds") List<Long> userIds);
}
