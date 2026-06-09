package com._s3k.runsync.domain.artrun.dto.response;

import com._s3k.runsync.entity.ArtRunSession;
import com._s3k.runsync.entity.enums.ArtRunStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@Schema(description = "협동 러닝 결과")
public class ArtRunResultRes {

    @Schema(description = "세션 ID", example = "100")
    private final Long sessionId;

    @Schema(description = "모집 제목", example = "강아지런 같이 그려요")
    private final String title;

    @Schema(description = "세션 상태", example = "COMPLETED")
    private final ArtRunStatus status;

    @Schema(description = "도안(원본) 좌표")
    private final List<CoordinateRes> designCoordinates;

    @Schema(description = "참가자별 결과")
    private final List<ArtRunResultParticipantRes> participants;

    private ArtRunResultRes(Long sessionId, String title, ArtRunStatus status,
                           List<CoordinateRes> designCoordinates, List<ArtRunResultParticipantRes> participants) {
        this.sessionId = sessionId;
        this.title = title;
        this.status = status;
        this.designCoordinates = designCoordinates;
        this.participants = participants;
    }

    public static ArtRunResultRes of(ArtRunSession session, List<ArtRunResultParticipantRes> participants) {
        List<CoordinateRes> designCoordinates = Arrays.stream(session.getRoutePath().getCoordinates())
                .map(c -> CoordinateRes.of(c.getY(), c.getX()))
                .toList();

        return new ArtRunResultRes(
                session.getId(),
                session.getTitle(),
                session.getStatus(),
                designCoordinates,
                participants
        );
    }
}
