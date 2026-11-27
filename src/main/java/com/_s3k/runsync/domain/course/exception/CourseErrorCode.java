package com._s3k.runsync.domain.course.exception;

import com._s3k.runsync.global.exception.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements ResultCode {

    OUT_OF_SERVICE_AREA(HttpStatus.BAD_REQUEST, 7001, "서비스 지역이 아닙니다. 현재는 대전 동구에서만 추천할 수 있습니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, 7002, "이 위치에서는 조건에 맞는 코스를 만들지 못했습니다."),
    RECOMMEND_SERVER_ERROR(HttpStatus.BAD_GATEWAY, 7003, "코스 추천 서버와 통신하지 못했습니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
}
