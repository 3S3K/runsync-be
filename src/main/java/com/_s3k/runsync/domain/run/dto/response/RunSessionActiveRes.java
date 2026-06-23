package com._s3k.runsync.domain.run.dto.response;

import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "진행 중 러닝 세션 조회 응답")
public class RunSessionActiveRes {

    @Schema(description = "세션 ID", example = "1")
    private final Long sessionId;

    @Schema(description = "세션 상태", example = "ACTIVE")
    private final RunningSessionStatus status;

    @Schema(description = "러닝 시작 시간", example = "2026-06-10T22:11:00")
    private final LocalDateTime startTime;

    @Schema(description = "연결된 협동 러닝 세션 ID (일반 러닝이면 null)", example = "100")
    private final Long artRunSessionId;

    @Schema(description = "누적 이동 거리 (km). 새로고침/재접속 시 이어뛰기 거리 복원용", example = "1.25")
    private final Double distance;

    private RunSessionActiveRes(Long sessionId, RunningSessionStatus status,
                               LocalDateTime startTime, Long artRunSessionId, Double distance) {
        this.sessionId = sessionId;
        this.status = status;
        this.startTime = startTime;
        this.artRunSessionId = artRunSessionId;
        this.distance = distance;
    }

    public static RunSessionActiveRes of(RunningSession session) {
        return new RunSessionActiveRes(
                session.getId(),
                session.getStatus(),
                session.getStartTime(),
                session.getArtRunSessionId(),
                session.getTotalDistance() != null ? session.getTotalDistance().doubleValue() : null
        );
    }
}
