package com._s3k.runsync.entity;

import com._s3k.runsync.entity.enums.ArtRunStatus;
import com._s3k.runsync.entity.enums.Provider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ArtRunSessionTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    @DisplayName("세션 생성 시 초기 상태가 RECRUITING이고 전달받은 값으로 조립된다")
    void of_initialStatusIsRecruiting() {
        // given
        User host = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        LocalDateTime meetingTime = LocalDateTime.now().plusDays(1);
        Point meetingPoint = GEOMETRY_FACTORY.createPoint(new Coordinate(127.1210, 37.5202));
        LineString routePath = GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                new Coordinate(127.1230, 37.5210),
                new Coordinate(127.1242, 37.5215)
        });

        // when
        ArtRunSession session = ArtRunSession.of(host, "강아지런", 5, meetingTime,
                "올림픽공원 평화의문 앞", meetingPoint, routePath);

        // then
        assertThat(session.getStatus()).isEqualTo(ArtRunStatus.RECRUITING);
        assertThat(session.getTitle()).isEqualTo("강아지런");
        assertThat(session.getCapacity()).isEqualTo(5);
        assertThat(session.getMeetingTime()).isEqualTo(meetingTime);
        assertThat(session.getMeetingPlaceName()).isEqualTo("올림픽공원 평화의문 앞");
        assertThat(session.getMeetingPoint()).isEqualTo(meetingPoint);
        assertThat(session.getRoutePath()).isEqualTo(routePath);
    }
}
