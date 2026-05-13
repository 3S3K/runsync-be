package com._s3k.runsync.domain.run.repository;

import com._s3k.runsync.domain.run.exception.RunSessionErrorCode;
import com._s3k.runsync.global.exception.GlobalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RunSessionRedisRepositoryImpl implements RunSessionRedisRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void appendPath(Long sessionId, double latitude, double longitude, double speed) {
        try {
            String pathJson = objectMapper.writeValueAsString(Map.of(
                    "lat", latitude,
                    "lng", longitude,
                    "speed", speed,
                    "recordedAt", Instant.now().toString()
            ));
            redisTemplate.opsForList().rightPush(pathKey(sessionId), pathJson);
            redisTemplate.expire(pathKey(sessionId), Duration.ofDays(1));
        } catch (JsonProcessingException e) {
            throw new GlobalException(RunSessionErrorCode.PATH_SERIALIZE_FAILED);
        }
    }

    @Override
    public List<String> getPaths(Long sessionId) {
        List<String> paths = redisTemplate.opsForList().range(pathKey(sessionId), 0, -1);
        return paths != null ? paths : List.of();
    }

    @Override
    public void deletePaths(Long sessionId) {
        redisTemplate.delete(pathKey(sessionId));
    }

    @Override
    public void deleteState(Long sessionId) {
        redisTemplate.delete(stateKey(sessionId));
    }

    private String pathKey(Long sessionId) {
        return "run_session:" + sessionId + ":paths";
    }

    private String stateKey(Long sessionId) {
        return "run_session:" + sessionId + ":state";
    }
}
