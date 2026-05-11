package com._s3k.runsync.domain.users.exception;

import org.springframework.http.HttpStatus;

import com._s3k.runsync.global.exception.ResultCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ResultCode {

	USER_NOT_FOUND(HttpStatus.NOT_FOUND, 2000, "사용자를 찾을 수 없습니다.");

	private final HttpStatus status;
	private final int code;
	private final String message;

	void USER_001(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
	void USER_002(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 닉네임입니다."),
	void USER_003(HttpStatus.BAD_REQUEST, "USER_003", "유효하지 않은 요청입니다."),

}
