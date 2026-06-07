package com._s3k.runsync.domain.friend.dto.response;

import com._s3k.runsync.entity.FriendRequest;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "친구 요청 수락 응답")
public class FriendAcceptRes {

    @Schema(description = "친구 요청 ID", example = "10")
    private Long requestId;

    @Schema(description = "요청 상태", example = "ACCEPTED")
    private FriendRequestStatus status;

    private FriendAcceptRes(Long requestId, FriendRequestStatus status) {
        this.requestId = requestId;
        this.status = status;
    }

    public static FriendAcceptRes of(FriendRequest friendRequest) {
        return new FriendAcceptRes(friendRequest.getId(), friendRequest.getStatus());
    }
}
