package com._s3k.runsync.domain.location.service;

import com._s3k.runsync.domain.friend.repository.FriendshipRepository;
import com._s3k.runsync.domain.location.dto.request.LocationUpdateReq;
import com._s3k.runsync.domain.location.exception.LocationErrorCode;
import com._s3k.runsync.domain.location.repository.LocationRepository;
import com._s3k.runsync.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final FriendshipRepository friendshipRepository;

    public void saveGeoLocation(Long userId, LocationUpdateReq data) {
        if (data == null || data.getLatitude() == null || data.getLongitude() == null) {
            throw new GlobalException(LocationErrorCode.INVALID_LOCATION_DATA);
        }

        locationRepository.saveGeoLocation(userId, data.getLongitude(), data.getLatitude());
    }

    public boolean canSubscribeFriendTopic(Long subscriberId, Long targetUserId) {
        return friendshipRepository.existsByUser_IdAndFriend_Id(subscriberId, targetUserId);
    }

    public void removeLocation(Long userId) {
        locationRepository.deleteGeoLocation(userId);
    }
}
