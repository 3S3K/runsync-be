package com._s3k.runsync.domain.run.exception;

import com._s3k.runsync.global.exception.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RunRecordErrorCode implements ResultCode {

    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 4001, "러닝 기록을 찾을 수 없습니다."),
    RECORD_NOT_OWNER(HttpStatus.FORBIDDEN, 4002, "해당 러닝 기록에 대한 권한이 없습니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
}
