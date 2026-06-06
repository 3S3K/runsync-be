package com._s3k.runsync.domain.friend.controller;

import com._s3k.runsync.domain.friend.dto.response.FriendListRes;
import com._s3k.runsync.domain.friend.dto.response.ReceivedFriendRequestRes;
import com._s3k.runsync.domain.friend.dto.response.SentFriendRequestRes;
import com._s3k.runsync.domain.friend.service.FriendService;
import com._s3k.runsync.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 목록 조회", description = "친구 목록 및 활동 상태 조회. 로그인 필요")
    @GetMapping
    public CommonResponse<List<FriendListRes>> getFriends(
            @AuthenticationPrincipal Long userId) {
        return CommonResponse.success(friendService.getFriendsByUserId(userId));
    }

    @Operation(summary = "보낸 친구 요청 목록 조회", description = "내가 보낸 PENDING 상태의 친구 요청 목록 조회. 로그인 필요")
    @GetMapping("/requests/sent")
    public CommonResponse<List<SentFriendRequestRes>> getSentFriendRequests(
            @AuthenticationPrincipal Long userId) {
        return CommonResponse.success(friendService.getSentFriendRequests(userId));
    }

    @Operation(summary = "받은 친구 요청 목록 조회", description = "내가 받은 PENDING 상태의 친구 요청 목록 조회. 로그인 필요")
    @GetMapping("/requests/received")
    public CommonResponse<List<ReceivedFriendRequestRes>> getReceivedFriendRequests(
            @AuthenticationPrincipal Long userId) {
        return CommonResponse.success(friendService.getReceivedFriendRequests(userId));
    }
}
