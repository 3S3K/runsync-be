package com._s3k.runsync.domain.users.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com._s3k.runsync.domain.users.service.UserService;
import com._s3k.runsync.domain.users.dto.request.UserUpdateReq;
import com._s3k.runsync.domain.users.dto.response.UserInfoRes;
import com._s3k.runsync.domain.users.dto.response.UserProfileRes;
import com._s3k.runsync.domain.users.dto.response.UserRecordsScrollRes;
import com._s3k.runsync.domain.users.dto.response.UserSummaryRes;
import com._s3k.runsync.domain.users.dto.response.UserUpdateRes;
import com._s3k.runsync.global.common.dto.CommonResponse;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "내 정보 조회: 로그인 필요")
    @GetMapping("/me")
    public CommonResponse<UserInfoRes> getMyInfo(
            @Parameter(description = "사용자 ID", required = true)
            @AuthenticationPrincipal Long userId
    ) {
        UserInfoRes response = userService.getMyInfo(userId);
        return CommonResponse.success(response);
    }

    @Operation(summary = "특정 사용자 조회", description = "특정 사용자 프로필 조회: 로그인 필요")
    @GetMapping("/{userId}")
    public CommonResponse<UserProfileRes> getUserById(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable("userId") Long targetUserId
    ) {
        return CommonResponse.success(userService.getUserById(targetUserId));
    }

    @Operation(summary = "내 정보 수정", description = "내 정보 수정: 로그인 필요")
    @PatchMapping("/me")
    public CommonResponse<UserUpdateRes> updateMyInfo(
            @Parameter(description = "사용자 ID", required = true)
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "닉네임, 프로필 이미지, 성별, 생년월일", required = true)
            @Valid @RequestBody UserUpdateReq request
    ) {
        UserUpdateRes response = userService.updateMyInfo(userId, request);
        return CommonResponse.success(response);
    }

    @Operation(summary = "개인페이지 프로필 + 월간 통계 조회", description = "내 프로필과 이번 달 러닝 통계 조회: 로그인 필요")
    @GetMapping("/me/summary")
    public CommonResponse<UserSummaryRes> getUserSummary(
            @Parameter(description = "사용자 ID", required = true)
            @AuthenticationPrincipal Long userId
    ) {
        return CommonResponse.success(userService.getUserSummary(userId));
    }

    @Operation(summary = "러닝 기록 목록 조회", description = "커서 기반 페이지네이션으로 러닝 기록 목록 조회: 로그인 필요")
    @GetMapping("/me/records")
    public CommonResponse<UserRecordsScrollRes> getUserRecords(
            @Parameter(description = "사용자 ID", required = true)
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "마지막으로 받은 recordId (첫 요청 시 생략)")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "조회할 기록 수 (기본값 5)")
            @RequestParam(defaultValue = "5") int size
    ) {
        return CommonResponse.success(userService.getUserRecords(userId, cursor, size));
    }
}
