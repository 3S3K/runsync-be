package com._s3k.runsync.domain.location.repository;

import com._s3k.runsync.domain.location.exception.LocationErrorCode;
import com._s3k.runsync.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void saveGeoLocation(Long userId, double longitude, double latitude) {
        try {
            redisTemplate.opsForGeo().add("user:locations", new Point(longitude, latitude), String.valueOf(userId));
        } catch (Exception e) {
            throw new GlobalException(LocationErrorCode.LOCATION_SAVE_FAILED);
        }
    }

    @Override
    public void deleteGeoLocation(Long userId) {
        redisTemplate.opsForGeo().remove("user:locations", String.valueOf(userId));
    }

}
