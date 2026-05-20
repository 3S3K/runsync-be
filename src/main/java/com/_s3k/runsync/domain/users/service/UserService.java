package com._s3k.runsync.domain.users.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.dto.request.UserUpdateRequest;
import com._s3k.runsync.domain.users.dto.response.RecordRes;
import com._s3k.runsync.domain.users.dto.response.UserInfoResponse;
import com._s3k.runsync.domain.users.dto.response.UserRecordsScrollRes;
import com._s3k.runsync.domain.users.dto.response.UserSummaryRes;
import com._s3k.runsync.domain.users.dto.response.UserUpdateResponse;
import com._s3k.runsync.global.common.ScrollPaginationCollection;
import com._s3k.runsync.global.exception.GlobalException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;
    private final RunRecordRepository runRecordRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_001));

        return UserInfoResponse.from(user);
    }

    @Transactional
    public UserUpdateResponse updateMyInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_001));

        // 사용자 정보 업데이트 로직
        // user.update(request); // 실제 업데이트 메서드는 User 엔티티에 구현되어 있을 것입니다

        // 닉네임 중복 체크 (실제 메서드명은 UserRepository 구현에 따라 다를 수 있습니다)
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new GlobalException(UserErrorCode.USER_002);
        }

        return UserUpdateResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserSummaryRes getUserSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalDistance = runRecordRepository.sumDistanceByUserIdAndMonth(userId, now.getYear(), now.getMonthValue());
        Long totalRunCount = runRecordRepository.countByUserIdAndMonth(userId, now.getYear(), now.getMonthValue());

        return UserSummaryRes.of(user, totalDistance.doubleValue(), totalRunCount.intValue());
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
}