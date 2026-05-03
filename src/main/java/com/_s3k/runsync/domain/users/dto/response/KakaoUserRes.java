package com._s3k.runsync.domain.users.dto.response;

import lombok.Getter;

@Getter
public class KakaoUserRes {

	private Long id;
	private KakaoAccount kakao_account;

	@Getter
	public static class KakaoAccount {
		private Profile profile;

		@Getter
		public static class Profile {
			private String nickname;
			private String profile_image_url;
		}
	}
}
