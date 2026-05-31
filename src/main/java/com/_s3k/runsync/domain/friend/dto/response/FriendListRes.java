package com._s3k.runsync.domain.friend.dto.response;

import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.ActivityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "친구 목록 응답")
public class FriendListRes {

    @Schema(description = "친구 사용자 ID", example = "2")
    private Long friendUserId;

    @Schema(description = "친구 닉네임", example = "한현우")
    private String nickname;

    @Schema(description = "친구 프로필 이미지", example = "https://...")
    private String profileImage;

    @Schema(description = "활동 상태 (RUNNING / OFFLINE)", example = "RUNNING")
    private String activityStatus;

    @Schema(description = "마지막 활동 시간. OFFLINE 상태에서는 마지막 러닝 종료 시각, RUNNING 상태에서는 null", example = "2026-04-10T15:00:00")
    private LocalDateTime lastActiveAt;

    private FriendListRes(User friend, ActivityStatus status, LocalDateTime lastActiveAt) {
        this.friendUserId = friend.getId();
        this.nickname = friend.getNickname();
        this.profileImage = friend.getProfileImage();
        this.activityStatus = status.name();
        this.lastActiveAt = lastActiveAt;
    }

    public static FriendListRes of(User friend, ActivityStatus status, LocalDateTime lastActiveAt) {
        return new FriendListRes(friend, status, lastActiveAt);
    }
}
