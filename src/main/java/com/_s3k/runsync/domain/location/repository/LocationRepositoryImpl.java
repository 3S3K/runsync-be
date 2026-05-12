package com._s3k.runsync.domain.location.repository;

import com._s3k.runsync.domain.location.exception.LocationErrorCode;
import com._s3k.runsync.global.exception.GlobalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void saveGeoLocation(Long userId, double longitude, double latitude) {
        try {
            redisTemplate.opsForGeo().add("user:locations", new Point(longitude, latitude), String.valueOf(userId));
        } catch (Exception e) {
            throw new GlobalException(LocationErrorCode.LOCATION_SAVE_FAILED);
        }
    }

    @Override
    public void appendPath(Long sessionId, double latitude, double longitude, double speed) {
        String pathJson = toJson(Map.of(
                "lat", latitude,
                "lng", longitude,
                "speed", speed,
                "recordedAt", Instant.now().toString()
        ));
        String key = "run_session:" + sessionId + ":paths";
        try {
            redisTemplate.opsForList().rightPush(key, pathJson);
            redisTemplate.expire(key, java.time.Duration.ofDays(1));
        } catch (Exception e) {
            throw new GlobalException(LocationErrorCode.LOCATION_SAVE_FAILED);
        }
    }

    @Override
    public Set<String> findFriendIds(Long userId) {
        return redisTemplate.opsForSet().members("user:" + userId + ":friends");
    }

    @Override
    public void deleteGeoLocation(Long userId) {
        redisTemplate.opsForGeo().remove("user:locations", String.valueOf(userId));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new GlobalException(LocationErrorCode.LOCATION_SAVE_FAILED);
        }
    }
}
