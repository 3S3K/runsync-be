package com._s3k.runsync.domain.users.controller;

public class UserController {

    /**
     * GET /api/users/me - 내 정보 조회
     */
    @GetMapping("/me")
    public CommonResponse<UserInfoResponse> getMyInfo(
            @AuthenticationPrincipal Long userId  // TODO: 실제 인증 방식에 맞게 수정 필요
    ) {
        UserInfoResponse response = userService.getMyInfo(userId);
        return CommonResponse.success(response); // TODO: 실제 메서드명 확인 후 수정
    }

    /**
     * PATCH /api/users/me - 내 정보 수정
     */
    @PatchMapping("/me")
    public CommonResponse<UserUpdateResponse> updateMyInfo(
            @AuthenticationPrincipal Long userId,  // TODO: 실제 인증 방식에 맞게 수정 필요
            @RequestBody UserUpdateRequest request
    ) {
        UserUpdateResponse response = userService.updateMyInfo(userId, request);
        return CommonResponse.success(response); // TODO: 실제 메서드명 확인 후 수정
    }


}
