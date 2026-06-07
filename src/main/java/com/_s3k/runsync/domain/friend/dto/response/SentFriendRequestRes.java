package com._s3k.runsync.domain.friend.dto.response;

import com._s3k.runsync.entity.FriendRequest;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "보낸 친구 요청 응답")
public class SentFriendRequestRes {

    @Schema(description = "친구 요청 ID", example = "11")
    private Long requestId;

    @Schema(description = "요청 받은 사용자 ID", example = "4")
    private Long receiverId;

    @Schema(description = "요청 받은 사용자 닉네임", example = "한현우")
    private String receiverNickname;

    @Schema(description = "요청 상태", example = "PENDING")
    private FriendRequestStatus status;

    @Schema(description = "요청 생성 시각", example = "2026-04-10T14:05:00")
    private LocalDateTime createdAt;

    private SentFriendRequestRes(Long requestId, Long receiverId, String receiverNickname,
                                 FriendRequestStatus status, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.receiverId = receiverId;
        this.receiverNickname = receiverNickname;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static SentFriendRequestRes of(FriendRequest friendRequest) {
        return new SentFriendRequestRes(
                friendRequest.getId(),
                friendRequest.getReceiver().getId(),
                friendRequest.getReceiver().getNickname(),
                friendRequest.getStatus(),
                friendRequest.getCreatedAt()
        );
    }
}
