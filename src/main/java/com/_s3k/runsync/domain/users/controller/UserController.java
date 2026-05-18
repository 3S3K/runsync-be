package com._s3k.runsync.domain.users.controller;

// Spring 관련 import
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

// Lombok
import lombok.RequiredArgsConstructor;

// 프로젝트 내부 클래스들
import com._s3k.runsync.domain.users.service.UserService;
import com._s3k.runsync.domain.users.dto.request.UserUpdateRequest;
import com._s3k.runsync.domain.users.dto.response.UserInfoResponse;
import com._s3k.runsync.domain.users.dto.response.UserUpdateResponse;
import com._s3k.runsync.global.common.dto.CommonResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class UserController {

    // 🌟 이 필드 선언이 userService 에러를 해결합니다!
    private final UserService userService;

    @GetMapping("/me")
    public CommonResponse<UserInfoResponse> getMyInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserInfoResponse response = userService.getMyInfo(userId);
        return CommonResponse.success(response);
    }

    @PatchMapping("/me")
    public CommonResponse<UserUpdateResponse> updateMyInfo(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserUpdateRequest request
    ) {
        UserUpdateResponse response = userService.updateMyInfo(userId, request);
        return CommonResponse.success(response);
    }
}