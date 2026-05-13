package com._s3k.runsync.domain.run.dto.response;

import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "러닝 세션 종료 응답")
public class RunSessionEndRes {

    @Schema(description = "세션 ID", example = "1")
    private final Long sessionId;

    @Schema(description = "변경된 세션 상태", example = "COMPLETED")
    private final RunningSessionStatus status;

    private RunSessionEndRes(Long sessionId, RunningSessionStatus status) {
        this.sessionId = sessionId;
        this.status = status;
    }

    public static RunSessionEndRes of(RunningSession session) {
        return new RunSessionEndRes(session.getId(), session.getStatus());
    }
}
