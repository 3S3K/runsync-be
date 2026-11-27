package com._s3k.runsync.domain.course.service;

import com._s3k.runsync.domain.course.api.RecommendClient;
import com._s3k.runsync.domain.course.dto.request.AiRecommendReq;
import com._s3k.runsync.domain.course.dto.request.CourseRecommendReq;
import com._s3k.runsync.domain.course.dto.response.CourseRecommendRes;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseRecommendService {

    private static final int RECENT_RECORD_COUNT = 5;
    // 추천 서버는 거리와 시간이 0보다 큰 기록만 받는다. 그런 기록을 걸러내고도 5개를 채우기 위해 넉넉히 읽는다
    private static final int RECENT_RECORD_FETCH_SIZE = 20;

    private final UserRepository userRepository;
    private final RunRecordRepository runRecordRepository;
    private final RecommendClient recommendClient;
    private final Clock clock;

    // 외부 서버 호출 동안 DB 커넥션을 잡고 있지 않도록 메서드 전체를 트랜잭션으로 묶지 않는다.
    public CourseRecommendRes createRecommendation(Long userId, CourseRecommendReq request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        List<RunRecord> recentRecords = runRecordRepository
                .findByUserIdOrderByIdDesc(userId, PageRequest.of(0, RECENT_RECORD_FETCH_SIZE))
                .stream()
                .filter(RunRecord::hasDistanceAndDuration)
                .limit(RECENT_RECORD_COUNT)
                .toList();

        AiRecommendReq aiRequest = AiRecommendReq.of(request, user, LocalDate.now(clock), recentRecords);
        return CourseRecommendRes.of(recommendClient.recommend(aiRequest));
    }
}
