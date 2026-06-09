package com._s3k.runsync.domain.artrun.controller;

import com._s3k.runsync.domain.artrun.dto.request.ArtRunCreateReq;
import com._s3k.runsync.domain.artrun.dto.request.ArtRunStatusUpdateReq;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunCreateRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunDetailRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunResultRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunScrollRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunStatusRes;
import com._s3k.runsync.domain.artrun.service.ArtRunService;
import com._s3k.runsync.entity.enums.ArtRunStatus;
import com._s3k.runsync.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/art-runs")
@RequiredArgsConstructor
@Validated
public class ArtRunController {

    private final ArtRunService artRunService;

    @PostMapping
    @Operation(summary = "협동 러닝 세션 생성", description = "도안 좌표와 모임 정보로 협동 러닝 세션을 생성합니다. 로그인 필요")
    public CommonResponse<ArtRunCreateRes> createArtRun(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ArtRunCreateReq request
    ) {
        return CommonResponse.success(artRunService.createArtRun(userId, request));
    }

    @GetMapping
    @Operation(summary = "협동 러닝 세션 목록 조회", description = "커서 기반 페이지네이션으로 상태별 협동 러닝 세션 목록을 조회합니다. 로그인 필요")
    public CommonResponse<ArtRunScrollRes> getAllArtRuns(
            @RequestParam(defaultValue = "RECRUITING") ArtRunStatus status,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return CommonResponse.success(artRunService.getAllArtRuns(status, cursor, size));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "협동 러닝 세션 상세 조회", description = "협동 러닝 세션 상세 정보를 조회합니다. 로그인 필요")
    public CommonResponse<ArtRunDetailRes> getArtRunById(
            @PathVariable Long sessionId
    ) {
        return CommonResponse.success(artRunService.getArtRunById(sessionId));
    }

    @GetMapping("/{sessionId}/result")
    @Operation(summary = "협동 러닝 결과 조회", description = "종료된 협동 러닝의 참가자별 기록(경로/거리/시간)과 도안을 조회합니다. 참가자/호스트만 가능. 로그인 필요")
    public CommonResponse<ArtRunResultRes> getArtRunResult(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId
    ) {
        return CommonResponse.success(artRunService.getArtRunResultBySessionId(userId, sessionId));
    }

    @PostMapping("/{sessionId}/participants")
    @Operation(summary = "협동 러닝 세션 참가", description = "협동 러닝 세션에 참가합니다(신청 즉시 입장). 모집 중인 세션만 가능. 로그인 필요")
    public CommonResponse<Void> joinArtRun(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId
    ) {
        artRunService.joinArtRun(userId, sessionId);
        return CommonResponse.success(null);
    }

    @DeleteMapping("/{sessionId}/participants/me")
    @Operation(summary = "협동 러닝 세션 참가 취소", description = "본인의 협동 러닝 세션 참가를 취소합니다. 호스트는 취소할 수 없습니다. 로그인 필요")
    public CommonResponse<Void> leaveArtRun(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId
    ) {
        artRunService.leaveArtRun(userId, sessionId);
        return CommonResponse.success(null);
    }

    @PatchMapping("/{sessionId}")
    @Operation(summary = "협동 러닝 세션 상태 변경", description = "호스트가 세션을 시작/종료합니다. RECRUITING→IN_PROGRESS→COMPLETED 순서. 로그인 필요")
    public CommonResponse<ArtRunStatusRes> updateArtRunStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @Valid @RequestBody ArtRunStatusUpdateReq request
    ) {
        return CommonResponse.success(artRunService.updateArtRunStatus(userId, sessionId, request));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "협동 러닝 세션 삭제", description = "호스트가 모집 중인 세션을 삭제합니다. 로그인 필요")
    public CommonResponse<Void> deleteArtRun(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId
    ) {
        artRunService.deleteArtRun(userId, sessionId);
        return CommonResponse.success(null);
    }
}
