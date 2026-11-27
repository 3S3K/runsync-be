package com._s3k.runsync.domain.course.controller;

import com._s3k.runsync.domain.course.dto.request.CourseRecommendReq;
import com._s3k.runsync.domain.course.dto.response.CourseRecommendRes;
import com._s3k.runsync.domain.course.dto.response.RunnerSummaryRes;
import com._s3k.runsync.domain.course.service.CourseRecommendService;
import com._s3k.runsync.domain.course.service.RunnerSummaryService;
import com._s3k.runsync.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRecommendService courseRecommendService;
    private final RunnerSummaryService runnerSummaryService;

    @PostMapping("/recommendations")
    @Operation(summary = "AI 코스 추천",
            description = "출발 좌표·목표 거리·강도를 받아 순환 러닝 코스를 최대 3개 추천합니다. "
                    + "최근 러닝 기록 5회와 나이·성별로 코스별 예상 운동 강도를 계산합니다. 로그인 필요")
    public CommonResponse<CourseRecommendRes> createRecommendation(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CourseRecommendReq request
    ) {
        return CommonResponse.success(courseRecommendService.createRecommendation(userId, request));
    }

    @GetMapping("/runner-summary")
    @Operation(summary = "내 러닝 분석",
            description = "코스 추천에 반영되는 최근 러닝 기록 5회를 요약합니다. "
                    + "평균 페이스·심박·케이던스, 평균 거리, 최근 7일 거리, 체력 수준을 돌려줍니다. "
                    + "기록이 없으면 recordCount가 0이고 평균값은 null입니다. 로그인 필요")
    public CommonResponse<RunnerSummaryRes> getRunnerSummary(
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(runnerSummaryService.getRunnerSummaryByUserId(userId));
    }
}
