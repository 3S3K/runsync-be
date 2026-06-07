package com._s3k.runsync.domain.friend.dto.response;

import com._s3k.runsync.entity.FriendRequest;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "받은 친구 요청 응답")
public class ReceivedFriendRequestRes {

    @Schema(description = "친구 요청 ID", example = "1")
    private Long requestId;

    @Schema(description = "요청 보낸 사용자 ID", example = "2")
    private Long senderId;

    @Schema(description = "요청 보낸 사용자 닉네임", example = "runner123")
    private String senderNickname;

    @Schema(description = "요청 상태", example = "PENDING")
    private FriendRequestStatus status;

    @Schema(description = "요청 생성 시각", example = "2026-06-01T10:00:00")
    private LocalDateTime createdAt;

    private ReceivedFriendRequestRes(Long requestId, Long senderId, String senderNickname,
                                     FriendRequestStatus status, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ReceivedFriendRequestRes of(FriendRequest friendRequest) {
        return new ReceivedFriendRequestRes(
                friendRequest.getId(),
                friendRequest.getSender().getId(),
                friendRequest.getSender().getNickname(),
                friendRequest.getStatus(),
                friendRequest.getCreatedAt()
        );
    }
}
