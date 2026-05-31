package com._s3k.runsync.domain.run.repository;

import com._s3k.runsync.entity.RunRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RunRecordRepository extends JpaRepository<RunRecord, Long> {

    Optional<RunRecord> findByRunningSessionId(Long sessionId);

    @Query("SELECT COALESCE(SUM(r.distance), 0) AS totalDistance, COUNT(r) AS totalRunCount FROM RunRecord r WHERE r.user.id = :userId AND r.startTime >= :start AND r.startTime < :end")
    MonthlyStatsProjection findMonthlyStats(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<RunRecord> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    List<RunRecord> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long cursor, Pageable pageable);
}
