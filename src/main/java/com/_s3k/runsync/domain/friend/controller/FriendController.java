package com._s3k.runsync.domain.friend.controller;

import com._s3k.runsync.domain.friend.dto.request.FriendRequestReq;
import com._s3k.runsync.domain.friend.dto.response.FriendListRes;
import com._s3k.runsync.domain.friend.dto.response.FriendRequestRes;
import com._s3k.runsync.domain.friend.dto.response.ReceivedFriendRequestRes;
import com._s3k.runsync.domain.friend.service.FriendService;
import com._s3k.runsync.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 요청 보내기", description = "특정 사용자에게 친구 요청 전송. 로그인 필요")
    @PostMapping("/requests")
    public CommonResponse<FriendRequestRes> createFriendRequest(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody FriendRequestReq req) {
        return CommonResponse.success(friendService.createFriendRequest(userId, req));
    }

    @Operation(summary = "친구 목록 조회", description = "친구 목록 및 활동 상태 조회. 로그인 필요")
    @GetMapping
    public CommonResponse<List<FriendListRes>> getFriends(
            @AuthenticationPrincipal Long userId) {
        return CommonResponse.success(friendService.getFriendsByUserId(userId));
    }

    @Operation(summary = "받은 친구 요청 목록 조회", description = "내가 받은 PENDING 상태의 친구 요청 목록 조회. 로그인 필요")
    @GetMapping("/requests/received")
    public CommonResponse<List<ReceivedFriendRequestRes>> getReceivedFriendRequests(
            @AuthenticationPrincipal Long userId) {
        return CommonResponse.success(friendService.getReceivedFriendRequests(userId));
    }
}
