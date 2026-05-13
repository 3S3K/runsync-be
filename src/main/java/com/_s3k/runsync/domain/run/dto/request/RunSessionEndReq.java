package com._s3k.runsync.domain.run.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "러닝 종료 요청")
public class RunSessionEndReq {

    @NotNull
    @Schema(description = "러닝 종료 시간", example = "2026-05-13T09:00:00")
    private LocalDateTime endTime;

    @NotNull
    @PositiveOrZero
    @Schema(description = "최종 이동 거리(km)", example = "10.09")
    private Double totalDistance;
}
