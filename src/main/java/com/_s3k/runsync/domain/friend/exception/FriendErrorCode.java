package com._s3k.runsync.domain.friend.exception;

import org.springframework.http.HttpStatus;

import com._s3k.runsync.global.exception.ResultCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FriendErrorCode implements ResultCode {

    FRIEND_SELF_REQUEST(HttpStatus.BAD_REQUEST, 6000, "자기 자신에게 친구 요청을 보낼 수 없습니다."),
    FRIEND_REQUEST_ALREADY_SENT(HttpStatus.CONFLICT, 6001, "이미 친구 요청을 보냈습니다."),
    FRIEND_ALREADY_EXISTS(HttpStatus.CONFLICT, 6002, "이미 친구 관계입니다."),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, 6003, "친구 요청을 찾을 수 없습니다."),
    FRIEND_REQUEST_ALREADY_PROCESSED(HttpStatus.CONFLICT, 6004, "이미 처리된 친구 요청입니다."),
    FRIEND_REQUEST_RECEIVED(HttpStatus.CONFLICT, 6005, "상대방에게서 받은 친구 요청이 있습니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
}
