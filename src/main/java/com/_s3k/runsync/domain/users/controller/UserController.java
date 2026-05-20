package com._s3k.runsync.domain.users.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com._s3k.runsync.domain.users.service.UserService;
import com._s3k.runsync.domain.users.dto.request.UserUpdateReq;
import com._s3k.runsync.domain.users.dto.response.UserInfoRes;
import com._s3k.runsync.domain.users.dto.response.UserUpdateRes;
import com._s3k.runsync.global.common.dto.CommonResponse;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회
     * @param userId 사용자 ID
     */
    @Operation(summary = "내 정보 조회", description = "내 정보 조회: 로그인 필요")
    @GetMapping("/me")
    public CommonResponse<UserInfoRes> getMyInfo(
            @Parameter(description = "사용자 ID", required = true)
            @AuthenticationPrincipal Long userId
    ) {
        UserInfoRes response = userService.getMyInfo(userId);
        return CommonResponse.success(response);
    }

    /**
     * 내 정보 수정
     * @param userId 사용자 ID
     * @param request 수정할 사용자 정보
     */
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
}