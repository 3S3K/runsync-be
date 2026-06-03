package com._s3k.runsync.domain.artrun.exception;

import com._s3k.runsync.global.exception.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ArtRunErrorCode implements ResultCode {

    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, 5001, "협동 러닝 세션을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
}
