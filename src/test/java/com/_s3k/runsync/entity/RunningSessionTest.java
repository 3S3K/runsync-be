package com._s3k.runsync.entity;

import com._s3k.runsync.entity.enums.Provider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RunningSessionTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    @DisplayName("위치 업데이트 시 lastLocation, totalDistance, currentDurationTime이 갱신된다")
    void updateLocation() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(127.4562, 36.3321));

        // when
        session.updateLocation(point, BigDecimal.valueOf(2.34), 754);

        // then
        assertThat(session.getLastLocation()).isEqualTo(point);
        assertThat(session.getTotalDistance()).isEqualByComparingTo(BigDecimal.valueOf(2.34));
        assertThat(session.getCurrentDurationTime()).isEqualTo(754);
    }
}
