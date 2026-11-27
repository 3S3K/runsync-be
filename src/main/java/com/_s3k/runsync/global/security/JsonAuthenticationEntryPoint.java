package com._s3k.runsync.global.security;

import com._s3k.runsync.domain.users.exception.AuthErrorCode;
import com._s3k.runsync.global.common.dto.CommonResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증되지 않은 요청(토큰 누락·만료)에 401 과 공통 응답 형식을 돌려준다.
 * 지정하지 않으면 Spring Security 기본값이 본문 없는 403 이라, 클라이언트가 토큰 재발급 시점을 알 수 없다.
 */
@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        AuthErrorCode errorCode = AuthErrorCode.AUTHENTICATION_REQUIRED;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), new CommonResponse<Void>(errorCode));
    }
}
