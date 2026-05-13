package com._s3k.runsync.domain.run.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "러닝 종료 요청")
public class RunSessionEndReq {

    @Schema(description = "러닝 종료 시간", example = "2026-05-13T09:00:00")
    private LocalDateTime endTime;

    @Schema(description = "최종 이동 거리(km)", example = "10.09")
    private Double totalDistance;
}
