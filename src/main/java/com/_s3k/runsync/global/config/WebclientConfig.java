package com._s3k.runsync.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebclientConfig {

	@Bean
	public WebClient kakaoAuthWebClient(){
		return WebClient.builder()
			.baseUrl("https://kauth.kakao.com")
			.build();
	}

	@Bean
	public WebClient kakaoApiWebClient(){
		return WebClient.builder()
			.baseUrl("https://kapi.kakao.com")
			.build();
	}

	// AI 코스 추천 서버(runsync-ai). 환경변수 AI_SERVER_URL로 바꾼다
	@Bean
	public WebClient aiServerWebClient(@Value("${ai.server-url:http://localhost:8001}") String aiServerUrl){
		return WebClient.builder()
			.baseUrl(aiServerUrl)
			.build();
	}
}
