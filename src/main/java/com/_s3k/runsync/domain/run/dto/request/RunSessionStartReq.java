package com._s3k.runsync.domain.run.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "러닝 세션 시작 요청")
public class RunSessionStartReq {

    @NotNull
    @Schema(description = "러닝 시작 시간", example = "2026-04-10T22:11:00")
    private LocalDateTime startTime;
}
