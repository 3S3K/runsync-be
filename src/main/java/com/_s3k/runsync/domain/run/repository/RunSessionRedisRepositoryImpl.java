package com._s3k.runsync.domain.run.repository;

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
        String key = "run_session:" + sessionId + ":paths";
        try {
            String pathJson = objectMapper.writeValueAsString(Map.of(
                    "lat", latitude,
                    "lng", longitude,
                    "speed", speed,
                    "recordedAt", Instant.now().toString()
            ));
            redisTemplate.opsForList().rightPush(key, pathJson);
            redisTemplate.expire(key, Duration.ofDays(1));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("경로 직렬화 실패", e);
        }
    }

    @Override
    public List<String> getPaths(Long sessionId) {
        List<String> paths = redisTemplate.opsForList().range("run_session:" + sessionId + ":paths", 0, -1);
        return paths != null ? paths : List.of();
    }

    @Override
    public void deletePaths(Long sessionId) {
        redisTemplate.delete("run_session:" + sessionId + ":paths");
    }

    @Override
    public void deleteState(Long sessionId) {
        redisTemplate.delete("run_session:" + sessionId + ":state");
    }
}
