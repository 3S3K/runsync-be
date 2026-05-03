package com._s3k.runsync.global.security.jwt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtUserInfo {
	private Long userId;
	private String role;

	public static JwtUserInfo of(Long userId, String role) {
		return JwtUserInfo.builder()
			.userId(userId)
			.role(role)
			.build();
	}
}
