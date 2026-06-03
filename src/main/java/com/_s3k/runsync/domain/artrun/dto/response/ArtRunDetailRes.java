package com._s3k.runsync.domain.artrun.dto.response;

import com._s3k.runsync.entity.ArtRunSession;
import com._s3k.runsync.entity.enums.ArtRunStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@Schema(description = "협동 러닝 세션 상세")
public class ArtRunDetailRes {

    @Schema(description = "세션 ID", example = "100")
    private final Long sessionId;

    @Schema(description = "모집 제목", example = "강아지런 같이 그려요")
    private final String title;

    @Schema(description = "세션 상태", example = "RECRUITING")
    private final ArtRunStatus status;

    @Schema(description = "호스트 정보")
    private final ArtRunHostRes host;

    @Schema(description = "도안 좌표")
    private final List<CoordinateRes> coordinates;

    @Schema(description = "정원", example = "5")
    private final Integer capacity;

    @Schema(description = "현재 참가 인원", example = "2")
    private final Integer currentCount;

    @Schema(description = "모이는 시간", example = "2026-06-10T07:00:00")
    private final LocalDateTime meetingTime;

    @Schema(description = "모이는 장소")
    private final MeetingPlaceRes meetingPlace;

    @Schema(description = "참가자 목록")
    private final List<ParticipantRes> participants;

    private ArtRunDetailRes(Long sessionId, String title, ArtRunStatus status, ArtRunHostRes host,
                            List<CoordinateRes> coordinates, Integer capacity, Integer currentCount,
                            LocalDateTime meetingTime, MeetingPlaceRes meetingPlace, List<ParticipantRes> participants) {
        this.sessionId = sessionId;
        this.title = title;
        this.status = status;
        this.host = host;
        this.coordinates = coordinates;
        this.capacity = capacity;
        this.currentCount = currentCount;
        this.meetingTime = meetingTime;
        this.meetingPlace = meetingPlace;
        this.participants = participants;
    }

    public static ArtRunDetailRes of(ArtRunSession session, int currentCount, List<ParticipantRes> participants) {
        List<CoordinateRes> coordinates = Arrays.stream(session.getRoutePath().getCoordinates())
                .map(c -> CoordinateRes.of(c.getY(), c.getX()))
                .toList();

        return new ArtRunDetailRes(
                session.getId(),
                session.getTitle(),
                session.getStatus(),
                ArtRunHostRes.of(session.getHost()),
                coordinates,
                session.getCapacity(),
                currentCount,
                session.getMeetingTime(),
                MeetingPlaceRes.of(session),
                participants
        );
    }
}
