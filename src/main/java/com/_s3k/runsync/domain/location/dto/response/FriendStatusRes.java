package com._s3k.runsync.domain.location.dto.response;

import com._s3k.runsync.entity.enums.FriendStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "친구 상태 응답")
public class FriendStatusRes {

    @Schema(description = "친구 유저 ID", example = "2")
    private final Long friendId;

    @Schema(description = "상태", example = "RUNNING")
    private final FriendStatus status;

    private FriendStatusRes(Long friendId, FriendStatus status) {
        this.friendId = friendId;
        this.status = status;
    }

    public static FriendStatusRes of(Long friendId, FriendStatus status) {
        return new FriendStatusRes(friendId, status);
    }
}
