package com._s3k.runsync.domain.artrun.dto.response;

import com._s3k.runsync.entity.ArtRunSession;
import com._s3k.runsync.entity.enums.ArtRunStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "협동 러닝 세션 상태 변경 응답")
public class ArtRunStatusRes {

    @Schema(description = "세션 ID", example = "100")
    private final Long sessionId;

    @Schema(description = "변경된 세션 상태", example = "IN_PROGRESS")
    private final ArtRunStatus status;

    private ArtRunStatusRes(Long sessionId, ArtRunStatus status) {
        this.sessionId = sessionId;
        this.status = status;
    }

    public static ArtRunStatusRes of(ArtRunSession session) {
        return new ArtRunStatusRes(session.getId(), session.getStatus());
    }
}
