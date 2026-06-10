package com._s3k.runsync.domain.users.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.domain.friend.repository.FriendRequestRepository;
import com._s3k.runsync.domain.friend.repository.FriendshipRepository;
import com._s3k.runsync.domain.run.repository.MonthlyStatsProjection;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.entity.enums.FriendRequestStatus;
import com._s3k.runsync.entity.enums.Role;
import com._s3k.runsync.entity.enums.UserRelation;
import com._s3k.runsync.domain.users.dto.request.UserUpdateReq;
import com._s3k.runsync.domain.users.dto.response.RecordRes;
import com._s3k.runsync.domain.users.dto.response.UserInfoRes;
import com._s3k.runsync.domain.users.dto.response.UserProfileRes;
import com._s3k.runsync.domain.users.dto.response.UserRecordsScrollRes;
import com._s3k.runsync.domain.users.dto.response.UserSearchRes;
import com._s3k.runsync.domain.users.dto.response.UserSearchScrollRes;
import com._s3k.runsync.domain.users.dto.response.UserSummaryRes;
import com._s3k.runsync.domain.users.dto.response.UserUpdateRes;
import com._s3k.runsync.global.common.ScrollPaginationCollection;
import com._s3k.runsync.global.exception.GlobalException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RunRecordRepository runRecordRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public UserInfoRes getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        return UserInfoRes.of(user);
    }

    @Transactional(readOnly = true)
    public UserProfileRes getUserById(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        return UserProfileRes.of(user);
    }

    @Transactional
    public UserUpdateRes updateMyInfo(Long userId, UserUpdateReq request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        if (request.getNickname() != null &&
                userRepository.existsByNicknameAndIdNot(request.getNickname(), userId)) {
            throw new GlobalException(UserErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        try {
            user.updateInfo(request.getNickname(), request.getProfileImage(), request.getGender(), request.getBirthDate());
        } catch (DataIntegrityViolationException e) {
            throw new GlobalException(UserErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        if (user.getRole() == Role.TMP_USER && user.isProfileComplete()) {
            user.upgradeToUser();
        }

        return UserUpdateRes.of(user);
    }

    @Transactional(readOnly = true)
    public UserSummaryRes getUserSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        MonthlyStatsProjection stats = runRecordRepository.findMonthlyStats(userId, start, end);
        return UserSummaryRes.of(user, stats.getTotalDistance().doubleValue(),
                stats.getTotalRunCount().intValue(), stats.getTotalDurationSeconds().intValue());
    }

    @Transactional(readOnly = true)
    public UserRecordsScrollRes getUserRecords(Long userId, Long cursor, int size) {
        List<RunRecord> records = cursor == null
                ? runRecordRepository.findByUserIdOrderByIdDesc(userId, PageRequest.of(0, size + 1))
                : runRecordRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, cursor, PageRequest.of(0, size + 1));

        List<RecordRes> recordResList = records.stream()
                .map(RecordRes::of)
                .collect(Collectors.toList());

        return UserRecordsScrollRes.of(ScrollPaginationCollection.of(recordResList, size));
    }

    @Transactional(readOnly = true)
    public UserSearchScrollRes searchUsersByNickname(Long userId, String nickname, Long cursor, int size) {
        String escapedNickname = nickname.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        List<User> users = userRepository.searchByNickname(userId, escapedNickname, cursor, PageRequest.of(0, size + 1));
        if (users.isEmpty()) {
            return UserSearchScrollRes.of(ScrollPaginationCollection.of(List.of(), size));
        }

        List<Long> targetIds = users.stream().map(User::getId).toList();
        Set<Long> friendIds = new HashSet<>(friendshipRepository.findFriendIdsByUserIdAndFriendIdIn(userId, targetIds));
        Set<Long> sentIds = new HashSet<>(friendRequestRepository.findReceiverIdsBySenderAndStatus(userId, targetIds, FriendRequestStatus.PENDING));
        Set<Long> receivedIds = new HashSet<>(friendRequestRepository.findSenderIdsByReceiverAndStatus(userId, targetIds, FriendRequestStatus.PENDING));

        List<UserSearchRes> searchResList = users.stream()
                .map(user -> UserSearchRes.of(user, resolveRelation(user.getId(), friendIds, sentIds, receivedIds)))
                .collect(Collectors.toList());

        return UserSearchScrollRes.of(ScrollPaginationCollection.of(searchResList, size));
    }

    private UserRelation resolveRelation(Long targetId, Set<Long> friendIds, Set<Long> sentIds, Set<Long> receivedIds) {
        if (friendIds.contains(targetId)) {
            return UserRelation.FRIEND;
        }
        if (sentIds.contains(targetId)) {
            return UserRelation.REQUEST_SENT;
        }
        if (receivedIds.contains(targetId)) {
            return UserRelation.REQUEST_RECEIVED;
        }
        return UserRelation.NONE;
    }
}
