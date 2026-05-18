package com._s3k.runsync.domain.run.repository;

import java.util.List;

public interface RunSessionRedisRepository {

    void appendPath(Long sessionId, double latitude, double longitude, double speed);

    List<String> getPaths(Long sessionId);

    void deletePaths(Long sessionId);

    void deleteState(Long sessionId);
}
