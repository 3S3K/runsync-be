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

    public void saveGeoLocation(Long userId, LocationUpdateReq data) {
        if (data == null || data.getLatitude() == null || data.getLongitude() == null) {
            throw new GlobalException(LocationErrorCode.INVALID_LOCATION_DATA);
        }

        locationRepository.saveGeoLocation(userId, data.getLongitude(), data.getLatitude());
    }

    public Set<String> getFriendIds(Long userId) {
        return locationRepository.findFriendIds(userId);
    }

    public void removeLocation(Long userId) {
        locationRepository.deleteGeoLocation(userId);
    }
}
