package com._s3k.runsync.domain.friend.dto.response;

import com._s3k.runsync.entity.FriendRequest;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "친구 요청 거절 응답")
public class FriendRejectRes {

    @Schema(description = "친구 요청 ID", example = "10")
    private Long requestId;

    @Schema(description = "요청 상태", example = "REJECTED")
    private FriendRequestStatus status;

    private FriendRejectRes(Long requestId, FriendRequestStatus status) {
        this.requestId = requestId;
        this.status = status;
    }

    public static FriendRejectRes of(FriendRequest friendRequest) {
        return new FriendRejectRes(friendRequest.getId(), friendRequest.getStatus());
    }
}
