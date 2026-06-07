package com._s3k.runsync.domain.friend.controller;

import com._s3k.runsync.domain.friend.dto.request.FriendRequestReq;
import com._s3k.runsync.domain.friend.dto.response.FriendAcceptRes;
import com._s3k.runsync.domain.friend.dto.response.FriendRejectRes;
import com._s3k.runsync.domain.friend.dto.response.FriendListRes;
import com._s3k.runsync.domain.friend.dto.response.FriendRequestRes;
import com._s3k.runsync.domain.friend.dto.response.ReceivedFriendRequestRes;
import com._s3k.runsync.domain.friend.dto.response.SentFriendRequestRes;
import com._s3k.runsync.domain.friend.service.FriendService;
import com._s3k.runsync.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(summary = "친구 요청 수락", description = "받은 친구 요청 수락. 로그인 필요")
    @PatchMapping("/requests/{requestId}/accept")
    public CommonResponse<FriendAcceptRes> acceptFriendRequest(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long requestId) {
        return CommonResponse.success(friendService.acceptFriendRequest(userId, requestId));
    }

    @Operation(summary = "친구 요청 거절", description = "받은 친구 요청 거절. 로그인 필요")
    @PatchMapping("/requests/{requestId}/reject")
    public CommonResponse<FriendRejectRes> rejectFriendRequest(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long requestId) {
        return CommonResponse.success(friendService.rejectFriendRequest(userId, requestId));
    }

    @Operation(summary = "친구 삭제", description = "친구 관계 삭제. 로그인 필요")
    @DeleteMapping("/{friendUserId}")
    public CommonResponse<Void> deleteFriend(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long friendUserId) {
        friendService.deleteFriend(userId, friendUserId);
        return CommonResponse.success(null);
    }

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
