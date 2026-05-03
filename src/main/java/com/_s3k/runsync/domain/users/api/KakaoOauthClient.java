package com._s3k.runsync.domain.users.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com._s3k.runsync.domain.users.dto.response.KakaoUserRes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoOauthClient {

	@Value("${kakao.client-id}")
	private String clientId;

	@Value("${kakao.redirect-uri}")
	private String redirectUri;

	private final WebClient kakaoAuthWebClient;
	private final WebClient kakaoApiWebClient;

	public String getAccessToken(String code) {
		return kakaoAuthWebClient.post()
			.uri("/oauth/token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(BodyInserters.fromFormData("grant_type", "authorization_code")
				.with("client_id", clientId)
				.with("redirect_uri", redirectUri)
				.with("code", code))
			.retrieve()
			.bodyToMono(Map.class)
			.map(res -> (String) res.get("access_token"))
			.block();
	}

	public KakaoUserRes getUserInfo(String accessToken){
		return kakaoApiWebClient.get()
			.uri("/v2/user/me")
			.headers(headers -> headers.setBearerAuth(accessToken))
			.retrieve()
			.bodyToMono(KakaoUserRes.class)
			.block();
	}
}
