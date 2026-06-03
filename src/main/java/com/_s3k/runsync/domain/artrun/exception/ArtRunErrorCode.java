package com._s3k.runsync.domain.artrun.exception;

import com._s3k.runsync.global.exception.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ArtRunErrorCode implements ResultCode {

    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, 5001, "협동 러닝 세션을 찾을 수 없습니다."),
    NOT_RECRUITING(HttpStatus.CONFLICT, 5002, "모집 중인 세션이 아닙니다."),
    SESSION_FULL(HttpStatus.CONFLICT, 5003, "정원이 가득 찼습니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, 5004, "이미 참가한 세션입니다."),
    NOT_PARTICIPANT(HttpStatus.NOT_FOUND, 5005, "참가 중인 세션이 아닙니다."),
    HOST_CANNOT_LEAVE(HttpStatus.CONFLICT, 5006, "호스트는 참가를 취소할 수 없습니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
}
