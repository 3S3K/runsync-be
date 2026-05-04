package com._s3k.runsync.domain.run.dto.response;

import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "러닝 세션 시작 응답")
public class RunSessionStartRes {

    @Schema(description = "세션 ID", example = "1")
    private Long sessionId;

    @Schema(description = "세션 상태", example = "ACTIVE")
    private RunningSessionStatus status;

    private RunSessionStartRes(Long sessionId, RunningSessionStatus status) {
        this.sessionId = sessionId;
        this.status = status;
    }

    public static RunSessionStartRes of(RunningSession session) {
        return new RunSessionStartRes(
                session.getId(),
                session.getStatus()
        );
    }
}
