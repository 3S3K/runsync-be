package com._s3k.runsync.domain.artrun.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "협동 러닝 참가자 위치")
public class ArtRunLocationRes {

    @Schema(description = "참가자 유저 ID", example = "10")
    private final Long userId;

    @Schema(description = "위도", example = "37.5665")
    private final Double latitude;

    @Schema(description = "경도", example = "126.9780")
    private final Double longitude;

    private ArtRunLocationRes(Long userId, Double latitude, Double longitude) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static ArtRunLocationRes of(Long userId, Double latitude, Double longitude) {
        return new ArtRunLocationRes(userId, latitude, longitude);
    }
}
