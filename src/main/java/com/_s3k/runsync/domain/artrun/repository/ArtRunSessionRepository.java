package com._s3k.runsync.domain.artrun.repository;

import com._s3k.runsync.entity.ArtRunSession;
import com._s3k.runsync.entity.enums.ArtRunStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtRunSessionRepository extends JpaRepository<ArtRunSession, Long> {

    @Query("SELECT s FROM ArtRunSession s JOIN FETCH s.host WHERE s.status = :status ORDER BY s.id DESC")
    List<ArtRunSession> findByStatusOrderByIdDesc(@Param("status") ArtRunStatus status, Pageable pageable);

    @Query("SELECT s FROM ArtRunSession s JOIN FETCH s.host WHERE s.status = :status AND s.id < :cursor ORDER BY s.id DESC")
    List<ArtRunSession> findByStatusAndIdLessThanOrderByIdDesc(@Param("status") ArtRunStatus status,
                                                               @Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT s FROM ArtRunSession s JOIN FETCH s.host WHERE s.id = :sessionId")
    Optional<ArtRunSession> findByIdWithHost(@Param("sessionId") Long sessionId);
}
