package com._s3k.runsync.domain.artrun.controller;

import com._s3k.runsync.domain.artrun.dto.request.ArtRunCreateReq;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunCreateRes;
import com._s3k.runsync.domain.artrun.service.ArtRunService;
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
@RequestMapping("/api/art-runs")
@RequiredArgsConstructor
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
}
