package com._s3k.runsync.domain.run.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "러닝 세션 시작 요청")
public class RunSessionStartReq {

    @NotNull
    @PastOrPresent
    @Schema(description = "러닝 시작 시간", example = "2026-04-10T22:11:00")
    private LocalDateTime startTime;
}
