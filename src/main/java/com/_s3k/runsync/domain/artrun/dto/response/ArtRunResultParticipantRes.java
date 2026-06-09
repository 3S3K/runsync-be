package com._s3k.runsync.domain.artrun.dto.response;

import com._s3k.runsync.domain.run.dto.response.RunPathRes;
import com._s3k.runsync.entity.ArtRunParticipant;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "협동 러닝 결과 - 참가자별 기록")
public class ArtRunResultParticipantRes {

    @Schema(description = "참가자 사용자 ID", example = "10")
    private final Long userId;

    @Schema(description = "닉네임", example = "현우")
    private final String nickname;

    @Schema(description = "프로필 이미지", example = "https://sample.com/1.png")
    private final String profileImage;

    @Schema(description = "뛴 거리(km)", example = "2.59")
    private final Double distance;

    @Schema(description = "소요 시간(초)", example = "1048")
    private final Integer durationSeconds;

    @Schema(description = "실제 뛴 GPS 경로")
    private final List<RunPathRes> paths;

    private ArtRunResultParticipantRes(Long userId, String nickname, String profileImage,
                                       Double distance, Integer durationSeconds, List<RunPathRes> paths) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.distance = distance;
        this.durationSeconds = durationSeconds;
        this.paths = paths;
    }

    public static ArtRunResultParticipantRes of(ArtRunParticipant participant, RunRecord record) {
        User user = participant.getUser();
        if (record == null) {
            return new ArtRunResultParticipantRes(user.getId(), user.getNickname(), user.getProfileImage(),
                    0.0, 0, List.of());
        }

        List<RunPathRes> paths = record.getPaths().stream()
                .map(RunPathRes::of)
                .toList();
        return new ArtRunResultParticipantRes(user.getId(), user.getNickname(), user.getProfileImage(),
                record.getDistance().doubleValue(), record.getDurationSeconds(), paths);
    }
}
