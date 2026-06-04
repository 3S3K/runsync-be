package com._s3k.runsync.domain.friend.dto.response;

import com._s3k.runsync.entity.FriendRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "친구 요청 보내기 응답")
public class FriendRequestRes {

    @Schema(description = "친구 요청 ID", example = "10")
    private Long requestId;

    @Schema(description = "요청 상태", example = "PENDING")
    private String status;

    private FriendRequestRes(Long requestId, String status) {
        this.requestId = requestId;
        this.status = status;
    }

    public static FriendRequestRes of(FriendRequest friendRequest) {
        return new FriendRequestRes(friendRequest.getId(), friendRequest.getStatus().name());
    }
}
