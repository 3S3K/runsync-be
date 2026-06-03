package com._s3k.runsync.domain.artrun.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "좌표")
public class CoordinateRes {

    @Schema(description = "위도", example = "37.5210")
    private final Double latitude;

    @Schema(description = "경도", example = "127.1230")
    private final Double longitude;

    private CoordinateRes(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static CoordinateRes of(double latitude, double longitude) {
        return new CoordinateRes(latitude, longitude);
    }
}
