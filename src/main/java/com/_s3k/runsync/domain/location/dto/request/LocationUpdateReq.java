package com._s3k.runsync.domain.location.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "위치 업데이트 요청")
public class LocationUpdateReq {

    @Schema(description = "러닝 세션 ID", example = "1")
    private Long sessionId;

    @Schema(description = "위도", example = "37.5665")
    private Double latitude;

    @Schema(description = "경도", example = "126.9780")
    private Double longitude;

    @Schema(description = "속도 (m/s)", example = "3.5")
    private Double speed;
}
