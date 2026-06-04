package com._s3k.runsync.domain.artrun.dto.response;

import com._s3k.runsync.entity.ArtRunParticipant;
import com._s3k.runsync.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "참가자")
public class ParticipantRes {

    @Schema(description = "참가자 사용자 ID", example = "1")
    private final Long userId;

    @Schema(description = "닉네임", example = "현우")
    private final String nickname;

    @Schema(description = "프로필 이미지", example = "https://sample.com/1.png")
    private final String profileImage;

    @Schema(description = "참가 시각", example = "2026-06-02T10:00:00")
    private final LocalDateTime joinedAt;

    private ParticipantRes(Long userId, String nickname, String profileImage, LocalDateTime joinedAt) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.joinedAt = joinedAt;
    }

    public static ParticipantRes of(ArtRunParticipant participant) {
        User user = participant.getUser();
        return new ParticipantRes(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                participant.getCreatedAt()
        );
    }
}
