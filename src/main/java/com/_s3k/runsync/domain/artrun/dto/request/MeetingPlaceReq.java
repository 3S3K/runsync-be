package com._s3k.runsync.domain.artrun.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "모이는 장소")
public class MeetingPlaceReq {

    @NotBlank
    @Schema(description = "장소 이름", example = "올림픽공원 평화의문 앞")
    private String name;

    @NotNull
    @DecimalMin("-90")
    @DecimalMax("90")
    @Schema(description = "장소 위도", example = "37.5202")
    private Double latitude;

    @NotNull
    @DecimalMin("-180")
    @DecimalMax("180")
    @Schema(description = "장소 경도", example = "127.1210")
    private Double longitude;
}
