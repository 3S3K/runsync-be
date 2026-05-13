package com._s3k.runsync.domain.run.exception;

import com._s3k.runsync.global.exception.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RunSessionErrorCode implements ResultCode {

    ACTIVE_SESSION_ALREADY_EXISTS(HttpStatus.CONFLICT, 3001, "이미 진행 중인 러닝 세션이 있습니다."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, 3002, "러닝 세션을 찾을 수 없습니다."),
    SESSION_NOT_OWNER(HttpStatus.FORBIDDEN, 3003, "해당 러닝 세션에 대한 권한이 없습니다."),
    SESSION_NOT_ACTIVE(HttpStatus.BAD_REQUEST, 3004, "진행 중인 러닝 세션이 아닙니다."),
    PATH_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3005, "러닝 경로 처리에 실패했습니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
}
