package com._s3k.runsync.domain.run.dto.request;

import com._s3k.runsync.global.common.json.ServiceZoneLocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    @Schema(description = "러닝 종료 시간. 시간대가 붙은 값(Z, +09:00)은 한국 시간으로 변환하고, 없으면 한국 시간으로 본다",
            example = "2026-05-13T00:00:00Z")
    @JsonDeserialize(using = ServiceZoneLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    @NotNull
    @PositiveOrZero
    @Schema(description = "최종 이동 거리(km)", example = "10.09")
    private Double totalDistance;
}
