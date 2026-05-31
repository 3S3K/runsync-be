package com._s3k.runsync.domain.run.controller;

import com._s3k.runsync.domain.run.dto.response.RunRecordRes;
import com._s3k.runsync.domain.run.service.RunRecordService;
import com._s3k.runsync.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/run-records")
@RequiredArgsConstructor
public class RunRecordController {

    private final RunRecordService runRecordService;

    @GetMapping("/{recordId}")
    @Operation(summary = "러닝 기록 상세 조회", description = "러닝 기록 상세 정보를 조회합니다. 로그인 필요")
    public CommonResponse<RunRecordRes> getRunRecord(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long recordId
    ) {
        return CommonResponse.success(runRecordService.getRunRecordById(userId, recordId));
    }
}
