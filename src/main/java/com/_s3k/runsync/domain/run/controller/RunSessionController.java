package com._s3k.runsync.domain.run.controller;

import com._s3k.runsync.domain.run.dto.request.LocationUpdateReq;
import com._s3k.runsync.domain.run.dto.request.RunSessionStartReq;
import com._s3k.runsync.domain.run.dto.response.RunSessionStartRes;
import com._s3k.runsync.domain.run.service.RunSessionService;
import com._s3k.runsync.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{sessionId}/location")
    @Operation(summary = "러닝 중 위치 저장", description = "러닝 세션 진행 중 현재 위치를 저장합니다. 로그인 필요")
    public CommonResponse<Void> updateLocation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @Valid @RequestBody LocationUpdateReq request
    ){
        runSessionService.updateLocation(userId, sessionId, request);
        return CommonResponse.success(null);
    }
}
