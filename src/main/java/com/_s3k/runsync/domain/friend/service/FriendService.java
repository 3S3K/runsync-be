package com._s3k.runsync.domain.friend.service;

import com._s3k.runsync.domain.friend.dto.response.FriendListRes;
import com._s3k.runsync.domain.friend.repository.FriendshipRepository;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.run.repository.RunningSessionRepository;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.FriendStatus;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final RunningSessionRepository runningSessionRepository;
    private final RunRecordRepository runRecordRepository;

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

        return friends.stream()
                .map(friend -> {
                    RunningSession activeSession = activeSessionMap.get(friend.getId());
                    if (activeSession != null) {
                        return FriendListRes.of(friend, FriendStatus.RUNNING, null);
                    }
                    RunRecord lastRecord = lastRecordMap.get(friend.getId());
                    return FriendListRes.of(friend, FriendStatus.OFFLINE,
                            lastRecord != null ? lastRecord.getLastActiveAt() : null);
                })
                .toList();
    }
}
