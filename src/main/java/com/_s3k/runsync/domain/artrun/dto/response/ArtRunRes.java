package com._s3k.runsync.domain.artrun.dto.response;

import com._s3k.runsync.entity.ArtRunSession;
import com._s3k.runsync.entity.enums.ArtRunStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "협동 러닝 세션 목록 항목")
public class ArtRunRes {

    @Schema(description = "세션 ID", example = "100")
    private final Long sessionId;

    @Schema(description = "모집 제목", example = "강아지런 같이 그려요")
    private final String title;

    @Schema(description = "세션 상태", example = "RECRUITING")
    private final ArtRunStatus status;

    @Schema(description = "호스트 닉네임", example = "현우")
    private final String hostNickname;

    @Schema(description = "정원", example = "5")
    private final Integer capacity;

    @Schema(description = "현재 참가 인원", example = "2")
    private final Integer currentCount;

    @Schema(description = "모이는 시간", example = "2026-06-10T07:00:00")
    private final LocalDateTime meetingTime;

    @Schema(description = "모이는 장소 이름", example = "올림픽공원 평화의문 앞")
    private final String meetingPlaceName;

    private ArtRunRes(Long sessionId, String title, ArtRunStatus status, String hostNickname,
                      Integer capacity, Integer currentCount, LocalDateTime meetingTime, String meetingPlaceName) {
        this.sessionId = sessionId;
        this.title = title;
        this.status = status;
        this.hostNickname = hostNickname;
        this.capacity = capacity;
        this.currentCount = currentCount;
        this.meetingTime = meetingTime;
        this.meetingPlaceName = meetingPlaceName;
    }

    public static ArtRunRes of(ArtRunSession session, int currentCount) {
        return new ArtRunRes(
                session.getId(),
                session.getTitle(),
                session.getStatus(),
                session.getHost().getNickname(),
                session.getCapacity(),
                currentCount,
                session.getMeetingTime(),
                session.getMeetingPlaceName()
        );
    }
}
