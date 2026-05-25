package com._s3k.runsync.domain.run.repository;

import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RunningSessionRepository extends JpaRepository<RunningSession, Long> {

    boolean existsByUserIdAndStatus(Long userId, RunningSessionStatus status);

    Optional<RunningSession> findByUserIdAndStatus(Long userId, RunningSessionStatus status);

    List<RunningSession> findByUserIdInAndStatus(List<Long> userIds, RunningSessionStatus status);
}
