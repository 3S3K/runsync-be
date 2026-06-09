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
    HOST_CANNOT_LEAVE(HttpStatus.CONFLICT, 5006, "호스트는 참가를 취소할 수 없습니다."),
    NOT_HOST(HttpStatus.FORBIDDEN, 5007, "호스트만 가능한 작업입니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, 5008, "잘못된 상태 변경입니다."),
    ARTRUN_NOT_IN_PROGRESS(HttpStatus.CONFLICT, 5009, "진행 중인 협동 러닝이 아닙니다."),
    RESULT_ACCESS_DENIED(HttpStatus.FORBIDDEN, 5010, "참가자만 결과를 조회할 수 있습니다."),
    ARTRUN_NOT_COMPLETED(HttpStatus.CONFLICT, 5011, "종료된 협동 러닝만 결과를 조회할 수 있습니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;
}
