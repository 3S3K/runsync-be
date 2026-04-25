package com._s3k.runsync.entity.enums;

import lombok.Getter;

@Getter
public enum FriendRequestStatus {
    PENDING,   // 대기
    ACCEPTED,  // 수락
    REJECTED,  // 거절
    BLOCKED    // 차단
}
