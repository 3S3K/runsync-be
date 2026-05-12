package com._s3k.runsync.domain.location.exception;

import com._s3k.runsync.global.exception.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LocationErrorCode implements ResultCode {

    LOCATION_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 4001, "위치 저장에 실패했습니다."),
    INVALID_LOCATION_DATA(HttpStatus.BAD_REQUEST, 4002, "유효하지 않은 위치 데이터입니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
}
