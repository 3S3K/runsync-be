package com._s3k.runsync.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ResultCode {
    SUCCESS(HttpStatus.OK, 0, "정상 처리 되었습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, 9001, "요청 값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 9000, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
}
