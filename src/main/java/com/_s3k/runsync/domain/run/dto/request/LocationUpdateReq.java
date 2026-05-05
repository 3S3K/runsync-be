package com._s3k.runsync.domain.run.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "러닝 중 위치 업데이트 요청")
public class LocationUpdateReq {

    @NotNull
    @DecimalMin("-90.0") @DecimalMax("90.0")
    @Schema(description = "현재(마지막) 위도", example = "36.3321")
    private Double lastLatitude;

    @NotNull
    @DecimalMin("-180.0") @DecimalMax("180.0")
    @Schema(description = "현재(마지막) 경도", example = "127.4562")
    private Double lastLongitude;

    @NotNull
    @Schema(description = "현재까지 뛴 거리 (km)", example = "2.34")
    private Double currentDistance;

    @NotNull
    @Schema(description = "현재까지 진행 시간 (초)", example = "754")
    private Integer currentDurationTime;
}
