package com._s3k.runsync.domain.artrun.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ArtRunCreateReqTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("유효한 요청은 위반이 없다")
    void valid_noViolations() {
        ArtRunCreateReq request = validRequest();

        Set<ConstraintViolation<ArtRunCreateReq>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("정원이 2명 미만이면 검증에 실패한다")
    void capacity_lessThanTwo() {
        ArtRunCreateReq request = validRequest();
        ReflectionTestUtils.setField(request, "capacity", 1);

        Set<ConstraintViolation<ArtRunCreateReq>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("capacity"));
    }

    @Test
    @DisplayName("좌표가 2개 미만이면 검증에 실패한다")
    void coordinates_lessThanTwo() {
        ArtRunCreateReq request = validRequest();
        ReflectionTestUtils.setField(request, "coordinates", List.of(coord(37.5210, 127.1230)));

        Set<ConstraintViolation<ArtRunCreateReq>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("coordinates"));
    }

    @Test
    @DisplayName("좌표 리스트에 null 원소가 있으면 검증에 실패한다")
    void coordinates_nullElement() {
        ArtRunCreateReq request = validRequest();
        List<CoordinateReq> coordinates = new ArrayList<>();
        coordinates.add(coord(37.5210, 127.1230));
        coordinates.add(null);
        ReflectionTestUtils.setField(request, "coordinates", coordinates);

        Set<ConstraintViolation<ArtRunCreateReq>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("coordinates"));
    }

    @Test
    @DisplayName("모이는 시간이 과거이면 검증에 실패한다")
    void meetingTime_past() {
        ArtRunCreateReq request = validRequest();
        ReflectionTestUtils.setField(request, "meetingTime", LocalDateTime.now().minusDays(1));

        Set<ConstraintViolation<ArtRunCreateReq>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("meetingTime"));
    }

    @Test
    @DisplayName("좌표 위도가 범위를 벗어나면 검증에 실패한다")
    void coordinate_latitudeOutOfRange() {
        ArtRunCreateReq request = validRequest();
        ReflectionTestUtils.setField(request, "coordinates", List.of(coord(999.0, 127.1230), coord(37.5215, 127.1242)));

        Set<ConstraintViolation<ArtRunCreateReq>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("latitude"));
    }

    @Test
    @DisplayName("모임 장소 경도가 범위를 벗어나면 검증에 실패한다")
    void meetingPlace_longitudeOutOfRange() {
        ArtRunCreateReq request = validRequest();
        MeetingPlaceReq place = new MeetingPlaceReq();
        ReflectionTestUtils.setField(place, "name", "올림픽공원 평화의문 앞");
        ReflectionTestUtils.setField(place, "latitude", 37.5202);
        ReflectionTestUtils.setField(place, "longitude", 999.0);
        ReflectionTestUtils.setField(request, "meetingPlace", place);

        Set<ConstraintViolation<ArtRunCreateReq>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("longitude"));
    }

    private ArtRunCreateReq validRequest() {
        MeetingPlaceReq place = new MeetingPlaceReq();
        ReflectionTestUtils.setField(place, "name", "올림픽공원 평화의문 앞");
        ReflectionTestUtils.setField(place, "latitude", 37.5202);
        ReflectionTestUtils.setField(place, "longitude", 127.1210);

        ArtRunCreateReq request = new ArtRunCreateReq();
        ReflectionTestUtils.setField(request, "title", "강아지런 같이 그려요");
        ReflectionTestUtils.setField(request, "capacity", 5);
        ReflectionTestUtils.setField(request, "meetingTime", LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(request, "meetingPlace", place);
        ReflectionTestUtils.setField(request, "coordinates", List.of(coord(37.5210, 127.1230), coord(37.5215, 127.1242)));
        return request;
    }

    private CoordinateReq coord(double latitude, double longitude) {
        CoordinateReq coordinate = new CoordinateReq();
        ReflectionTestUtils.setField(coordinate, "latitude", latitude);
        ReflectionTestUtils.setField(coordinate, "longitude", longitude);
        return coordinate;
    }
}
