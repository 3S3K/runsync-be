package com._s3k.runsync.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class GlobalException extends RuntimeException {
    private final ResultCode resultCode;

    public GlobalException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
}
