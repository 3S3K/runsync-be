package com._s3k.runsync.domain.location.repository;

import java.util.Set;

public interface LocationRepository {

    void saveGeoLocation(Long userId, double longitude, double latitude);

    Set<String> findFriendIds(Long userId);

    void deleteGeoLocation(Long userId);
}
