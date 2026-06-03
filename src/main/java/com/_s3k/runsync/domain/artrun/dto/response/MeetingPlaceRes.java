package com._s3k.runsync.domain.artrun.dto.response;

import com._s3k.runsync.entity.ArtRunSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "모이는 장소")
public class MeetingPlaceRes {

    @Schema(description = "장소 이름", example = "올림픽공원 평화의문 앞")
    private final String name;

    @Schema(description = "장소 위도", example = "37.5202")
    private final Double latitude;

    @Schema(description = "장소 경도", example = "127.1210")
    private final Double longitude;

    private MeetingPlaceRes(String name, Double latitude, Double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static MeetingPlaceRes of(ArtRunSession session) {
        return new MeetingPlaceRes(
                session.getMeetingPlaceName(),
                session.getMeetingPoint().getY(),
                session.getMeetingPoint().getX()
        );
    }
}
