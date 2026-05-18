package com._s3k.runsync.domain.run.controller;

import com._s3k.runsync.domain.run.dto.request.LocationUpdateReq;
import com._s3k.runsync.domain.run.dto.request.RunRecordDetailReq;
import com._s3k.runsync.domain.run.dto.request.RunSessionEndReq;
import com._s3k.runsync.domain.run.dto.request.RunSessionStartReq;
import com._s3k.runsync.domain.run.dto.response.RunRecordDetailRes;
import com._s3k.runsync.domain.run.dto.response.RunSessionEndRes;
import com._s3k.runsync.domain.run.dto.response.RunSessionStartRes;
import com._s3k.runsync.domain.run.service.RunSessionService;
import com._s3k.runsync.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PatchMapping("/{sessionId}/location")
    @Operation(summary = "러닝 중 위치 저장", description = "러닝 세션 진행 중 현재 위치를 저장합니다. 로그인 필요")
    public CommonResponse<Void> updateLocation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @Valid @RequestBody LocationUpdateReq request
    ) {
        runSessionService.updateLocation(userId, sessionId, request);
        return CommonResponse.success(null);
    }

    @PostMapping("/{sessionId}/records")
    @Operation(summary = "러닝 종료 후 세부 기록 입력 저장", description = "러닝 종료 후 세부 기록을 저장합니다. 로그인 필요")
    public CommonResponse<RunRecordDetailRes> saveRunRecordDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @Valid @RequestBody RunRecordDetailReq request
    ) {
        return CommonResponse.success(runSessionService.saveRunRecordDetail(userId, sessionId, request));
    }

    @PatchMapping("/{sessionId}")
    @Operation(summary = "러닝 종료", description = "러닝 세션을 종료합니다. 로그인 필요")
    public CommonResponse<RunSessionEndRes> endRunSession(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @Valid @RequestBody RunSessionEndReq request
    ) {
        return CommonResponse.success(runSessionService.endRunSession(userId, sessionId, request));
    }
}
