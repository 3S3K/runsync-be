package com._s3k.runsync.domain.run.controller;

import com._s3k.runsync.domain.run.dto.request.RunSessionStartReq;
import com._s3k.runsync.domain.run.dto.response.RunSessionStartRes;
import com._s3k.runsync.domain.run.service.RunSessionService;
import com._s3k.runsync.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/run-sessions")
@RequiredArgsConstructor
public class RunSessionController {

    private final RunSessionService runSessionService;

    @PostMapping
    @Operation(summary = "러닝 시작", description = "러닝 세션을 생성합니다. 로그인 필요")
    public CommonResponse<RunSessionStartRes> createRunSession(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RunSessionStartReq request
    ) {
        return CommonResponse.success(runSessionService.createRunSession(userId, request));
    }
}
