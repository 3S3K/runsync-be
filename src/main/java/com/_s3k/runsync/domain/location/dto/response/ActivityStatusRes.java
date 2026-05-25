package com._s3k.runsync.domain.location.dto.response;

import com._s3k.runsync.entity.enums.ActivityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "친구 상태 응답")
public class ActivityStatusRes {

    @Schema(description = "친구 유저 ID", example = "2")
    private final Long friendId;

    @Schema(description = "상태", example = "RUNNING")
    private final ActivityStatus status;

    private ActivityStatusRes(Long friendId, ActivityStatus status) {
        this.friendId = friendId;
        this.status = status;
    }

    public static ActivityStatusRes of(Long friendId, ActivityStatus status) {
        return new ActivityStatusRes(friendId, status);
    }
}
