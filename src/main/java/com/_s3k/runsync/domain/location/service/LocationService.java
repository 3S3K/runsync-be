package com._s3k.runsync.domain.location.service;

import com._s3k.runsync.domain.location.dto.request.LocationUpdateReq;
import com._s3k.runsync.domain.location.exception.LocationErrorCode;
import com._s3k.runsync.domain.location.repository.LocationRepository;
import com._s3k.runsync.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public void saveLocation(Long userId, LocationUpdateReq data) {
        if (data == null || data.getLatitude() == null || data.getLongitude() == null || data.getSessionId() == null) {
            throw new GlobalException(LocationErrorCode.INVALID_LOCATION_DATA);
        }

        locationRepository.saveGeoLocation(userId, data.getLongitude(), data.getLatitude());
        locationRepository.appendPath(
                data.getSessionId(),
                data.getLatitude(),
                data.getLongitude(),
                data.getSpeed() != null ? data.getSpeed() : 0.0
        );
    }

    public Set<String> getFriendIds(Long userId) {
        return locationRepository.findFriendIds(userId);
    }

    public void removeLocation(Long userId) {
        locationRepository.deleteGeoLocation(userId);
    }
}
