package com._s3k.runsync.domain.run.dto.request;

import com._s3k.runsync.global.common.json.ServiceZoneLocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "러닝 세션 시작 요청")
public class RunSessionStartReq {

    @NotNull
    @PastOrPresent
    @Schema(description = "러닝 시작 시간. 시간대가 붙은 값(Z, +09:00)은 한국 시간으로 변환하고, 없으면 한국 시간으로 본다",
            example = "2026-04-10T13:11:00Z")
    @JsonDeserialize(using = ServiceZoneLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    @Positive
    @Schema(description = "협동 러닝 세션 ID (협동 러닝 참가 시에만 전송, 일반 러닝은 생략)", example = "100")
    private Long artRunSessionId;
}
