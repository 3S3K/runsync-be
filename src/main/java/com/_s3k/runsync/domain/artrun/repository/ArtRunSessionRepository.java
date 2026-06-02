package com._s3k.runsync.domain.artrun.repository;

import com._s3k.runsync.entity.ArtRunSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtRunSessionRepository extends JpaRepository<ArtRunSession, Long> {
}
