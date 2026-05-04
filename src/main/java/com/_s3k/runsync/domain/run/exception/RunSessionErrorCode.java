package com._s3k.runsync.domain.run.exception;

import com._s3k.runsync.global.exception.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RunSessionErrorCode implements ResultCode {

    ACTIVE_SESSION_ALREADY_EXISTS(HttpStatus.CONFLICT, 3001, "이미 진행 중인 러닝 세션이 있습니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
}
