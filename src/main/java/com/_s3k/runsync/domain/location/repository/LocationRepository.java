package com._s3k.runsync.domain.location.repository;

public interface LocationRepository {

    void saveGeoLocation(Long userId, double longitude, double latitude);

    void deleteGeoLocation(Long userId);
}
