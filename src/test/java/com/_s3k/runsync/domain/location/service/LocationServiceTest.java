package com._s3k.runsync.domain.location.service;

import com._s3k.runsync.domain.location.dto.request.LocationUpdateReq;
import com._s3k.runsync.domain.location.exception.LocationErrorCode;
import com._s3k.runsync.domain.location.repository.LocationRepository;
import com._s3k.runsync.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @InjectMocks
    private LocationService locationService;

    @Mock
    private LocationRepository locationRepository;

    private LocationUpdateReq createReq(Long sessionId, Double latitude, Double longitude, Double speed) {
        LocationUpdateReq req = new LocationUpdateReq();
        ReflectionTestUtils.setField(req, "sessionId", sessionId);
        ReflectionTestUtils.setField(req, "latitude", latitude);
        ReflectionTestUtils.setField(req, "longitude", longitude);
        ReflectionTestUtils.setField(req, "speed", speed);
        return req;
    }

    @Test
    @DisplayName("GEO 위치 저장 성공")
    void saveGeoLocation_success() {
        // given
        LocationUpdateReq req = createReq(1L, 37.5665, 126.9780, 3.5);

        // when
        locationService.saveGeoLocation(1L, req);

        // then
        verify(locationRepository).saveGeoLocation(1L, 126.9780, 37.5665);
    }

    @Test
    @DisplayName("data가 null이면 예외 발생")
    void saveGeoLocation_dataNullThrowsException() {
        // when & then
        assertThatThrownBy(() -> locationService.saveGeoLocation(1L, null))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(LocationErrorCode.INVALID_LOCATION_DATA));

        verify(locationRepository, never()).saveGeoLocation(anyLong(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("위도가 null이면 예외 발생")
    void saveGeoLocation_latitudeNullThrowsException() {
        // given
        LocationUpdateReq req = createReq(1L, null, 126.9780, 3.5);

        // when & then
        assertThatThrownBy(() -> locationService.saveGeoLocation(1L, req))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(LocationErrorCode.INVALID_LOCATION_DATA));

        verify(locationRepository, never()).saveGeoLocation(anyLong(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("경도가 null이면 예외 발생")
    void saveGeoLocation_longitudeNullThrowsException() {
        // given
        LocationUpdateReq req = createReq(1L, 37.5665, null, 3.5);

        // when & then
        assertThatThrownBy(() -> locationService.saveGeoLocation(1L, req))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(LocationErrorCode.INVALID_LOCATION_DATA));

        verify(locationRepository, never()).saveGeoLocation(anyLong(), anyDouble(), anyDouble());
    }
}
