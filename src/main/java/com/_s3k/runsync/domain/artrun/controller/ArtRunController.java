package com._s3k.runsync.domain.artrun.controller;

import com._s3k.runsync.domain.artrun.dto.request.ArtRunCreateReq;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunCreateRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunDetailRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunScrollRes;
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
import org.springframework.web.bind.annotation.GetMapping;
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
}
