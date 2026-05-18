package com._s3k.runsync.domain.run.repository;

import com._s3k.runsync.entity.RunRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RunRecordRepository extends JpaRepository<RunRecord, Long> {

    Optional<RunRecord> findByRunningSessionId(Long sessionId);

    @Query("SELECT r FROM RunRecord r LEFT JOIN FETCH r.paths WHERE r.id = :recordId")
    Optional<RunRecord> findByIdWithPaths(@Param("recordId") Long recordId);
}
