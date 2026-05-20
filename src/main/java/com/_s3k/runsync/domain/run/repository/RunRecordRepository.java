package com._s3k.runsync.domain.run.repository;

import com._s3k.runsync.entity.RunRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RunRecordRepository extends JpaRepository<RunRecord, Long> {

    Optional<RunRecord> findByRunningSessionId(Long sessionId);

    @Query("SELECT COALESCE(SUM(r.distance), 0) FROM RunRecord r WHERE r.user.id = :userId AND year(r.startTime) = :year AND month(r.startTime) = :month")
    BigDecimal sumDistanceByUserIdAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(r) FROM RunRecord r WHERE r.user.id = :userId AND year(r.startTime) = :year AND month(r.startTime) = :month")
    Long countByUserIdAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    List<RunRecord> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    List<RunRecord> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long cursor, Pageable pageable);
}
