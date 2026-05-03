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
}
