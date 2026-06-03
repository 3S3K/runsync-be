package com._s3k.runsync.domain.artrun.repository;

import com._s3k.runsync.entity.ArtRunParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtRunParticipantRepository extends JpaRepository<ArtRunParticipant, Long> {

    boolean existsByArtRunSession_IdAndUser_Id(Long artRunSessionId, Long userId);

    int countByArtRunSession_Id(Long artRunSessionId);

    Optional<ArtRunParticipant> findByArtRunSession_IdAndUser_Id(Long artRunSessionId, Long userId);

    void deleteByArtRunSession_Id(Long artRunSessionId);

    @Query("SELECT p.artRunSession.id AS sessionId, COUNT(p) AS participantCount " +
            "FROM ArtRunParticipant p WHERE p.artRunSession.id IN :sessionIds GROUP BY p.artRunSession.id")
    List<ParticipantCountProjection> countBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    @Query("SELECT p FROM ArtRunParticipant p JOIN FETCH p.user WHERE p.artRunSession.id = :sessionId ORDER BY p.id ASC")
    List<ArtRunParticipant> findByArtRunSessionIdWithUser(@Param("sessionId") Long sessionId);
}
