package com._s3k.runsync.domain.artrun.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "좌표")
public class CoordinateReq {

    @NotNull
    @DecimalMin("-90")
    @DecimalMax("90")
    @Schema(description = "위도", example = "37.5210")
    private Double latitude;

    @NotNull
    @DecimalMin("-180")
    @DecimalMax("180")
    @Schema(description = "경도", example = "127.1230")
    private Double longitude;
}
