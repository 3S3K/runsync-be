package com._s3k.runsync.domain.users.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "카카오 로그인 요청")
public class KakaoLoginReq {

	@Schema(description = "카카오 인가 코드", example="")
	@NotBlank(message = "인가 코드는 필수입니다.")
	private String code;

}
