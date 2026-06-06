package com._s3k.runsync.domain.friend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "친구 요청 보내기 요청")
public class FriendRequestReq {

    @NotNull
    @Schema(description = "친구 요청 받을 사용자 ID", example = "2")
    private Long receiverId;

    public static FriendRequestReq of(Long receiverId) {
        FriendRequestReq req = new FriendRequestReq();
        req.receiverId = receiverId;
        return req;
    }
}
