package com._s3k.runsync.domain.course.controller;

import com._s3k.runsync.domain.course.dto.request.CourseRecommendReq;
import com._s3k.runsync.domain.course.dto.response.CourseRecommendRes;
import com._s3k.runsync.domain.course.service.CourseRecommendService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRecommendService courseRecommendService;

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
}
