package com._s3k.runsync.domain.friend.service;

import com._s3k.runsync.domain.friend.dto.request.FriendRequestReq;
import com._s3k.runsync.domain.friend.dto.response.FriendListRes;
import com._s3k.runsync.domain.friend.dto.response.FriendRequestRes;
import com._s3k.runsync.domain.friend.exception.FriendErrorCode;
import com._s3k.runsync.domain.friend.repository.FriendRequestRepository;
import com._s3k.runsync.domain.friend.repository.FriendshipRepository;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.run.repository.RunningSessionRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.FriendRequest;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.ActivityStatus;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import com._s3k.runsync.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final RunningSessionRepository runningSessionRepository;
    private final RunRecordRepository runRecordRepository;

    @Transactional
    public FriendRequestRes createFriendRequest(Long senderId, FriendRequestReq request) {
        Long receiverId = request.getReceiverId();

        if (senderId.equals(receiverId)) {
            throw new GlobalException(FriendErrorCode.FRIEND_SELF_REQUEST);
        }

        User sender = userRepository.findById(senderId)
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        User receiver = userRepository.findById(receiverId)
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        if (friendRequestRepository.existsBySender_IdAndReceiver_IdAndStatus(senderId, receiverId, FriendRequestStatus.PENDING)) {
            throw new GlobalException(FriendErrorCode.FRIEND_REQUEST_ALREADY_SENT);
        }

        if (friendshipRepository.existsByUser_IdAndFriend_Id(senderId, receiverId)) {
            throw new GlobalException(FriendErrorCode.FRIEND_ALREADY_EXISTS);
        }

        try {
            FriendRequest friendRequest = friendRequestRepository.save(FriendRequest.of(sender, receiver));
            return FriendRequestRes.of(friendRequest);
        } catch (DataIntegrityViolationException e) {
            throw new GlobalException(FriendErrorCode.FRIEND_REQUEST_ALREADY_SENT);
        }
    }

    @Transactional(readOnly = true)
    public List<FriendListRes> getFriendsByUserId(Long userId) {
        List<User> friends = friendshipRepository.findFriendsByUserId(userId);
        if (friends.isEmpty()) return Collections.emptyList();

        List<Long> friendIds = friends.stream().map(User::getId).toList();

        Map<Long, RunningSession> activeSessionMap = runningSessionRepository
                .findByUserIdInAndStatus(friendIds, RunningSessionStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        RunningSession::getUserId,
                        Function.identity(),
                        (a, b) -> a.getStartTime().isAfter(b.getStartTime()) ? a : b
                ));

        Map<Long, RunRecord> lastRecordMap = runRecordRepository
                .findLatestByUserIds(friendIds)
                .stream()
                .collect(Collectors.toMap(
                        RunRecord::getUserId,
                        Function.identity(),
                        (a, b) -> a.getId().compareTo(b.getId()) > 0 ? a : b
                ));

        record FriendInfo(User friend, ActivityStatus status, LocalDateTime lastActiveAt) {}

        return friends.stream()
                .map(friend -> {
                    RunningSession activeSession = activeSessionMap.get(friend.getId());
                    if (activeSession != null) {
                        return new FriendInfo(friend, ActivityStatus.RUNNING, null);
                    }
                    RunRecord lastRecord = lastRecordMap.get(friend.getId());
                    return new FriendInfo(friend, ActivityStatus.OFFLINE,
                            lastRecord != null ? lastRecord.getLastActiveAt() : null);
                })
                .sorted(Comparator
                        .comparing(FriendInfo::status)
                        .thenComparing(FriendInfo::lastActiveAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(info -> info.friend().getId()))
                .map(info -> FriendListRes.of(info.friend(), info.status(), info.lastActiveAt()))
                .toList();
    }
}
