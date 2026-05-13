package com._s3k.runsync.domain.location.repository;

import com._s3k.runsync.domain.location.exception.LocationErrorCode;
import com._s3k.runsync.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class LocationRepositoryImplTest {

    @InjectMocks
    private LocationRepositoryImpl locationRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("위치 Geo 저장 성공")
    void saveGeoLocation_success() {
        // given
        GeoOperations<String, String> geoOps = mock(GeoOperations.class);
        given(redisTemplate.opsForGeo()).willReturn(geoOps);

        // when
        locationRepository.saveGeoLocation(1L, 126.9780, 37.5665);

        // then
        verify(geoOps).add(anyString(), any(Point.class), anyString());
    }

    @Test
    @DisplayName("Geo 저장 중 Redis 오류 시 예외 발생")
    void saveGeoLocation_redisError_throwsException() {
        // given
        GeoOperations<String, String> geoOps = mock(GeoOperations.class);
        given(redisTemplate.opsForGeo()).willReturn(geoOps);
        given(geoOps.add(anyString(), any(Point.class), anyString())).willThrow(RuntimeException.class);

        // when & then
        assertThatThrownBy(() -> locationRepository.saveGeoLocation(1L, 126.9780, 37.5665))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(LocationErrorCode.LOCATION_SAVE_FAILED));
    }

    @Test
    @DisplayName("친구 ID 목록 조회 성공")
    void findFriendIds_success() {
        // given
        SetOperations<String, String> setOps = mock(SetOperations.class);
        given(redisTemplate.opsForSet()).willReturn(setOps);
        given(setOps.members("user:1:friends")).willReturn(Set.of("2", "3"));

        // when
        Set<String> result = locationRepository.findFriendIds(1L);

        // then
        assertThat(result).containsExactlyInAnyOrder("2", "3");
    }

    @Test
    @DisplayName("위치 Geo 삭제 성공")
    void deleteGeoLocation_success() {
        // given
        GeoOperations<String, String> geoOps = mock(GeoOperations.class);
        given(redisTemplate.opsForGeo()).willReturn(geoOps);

        // when
        locationRepository.deleteGeoLocation(1L);

        // then
        verify(geoOps).remove("user:locations", "1");
    }
}
