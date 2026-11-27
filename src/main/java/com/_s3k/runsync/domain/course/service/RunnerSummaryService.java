package com._s3k.runsync.domain.course.service;

import com._s3k.runsync.domain.course.dto.response.RunnerSummaryRes;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.entity.RunRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RunnerSummaryService {

    private static final int WEEK_DAYS = 7;

    private final RunRecordRepository runRecordRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public RunnerSummaryRes getRunnerSummaryByUserId(Long userId) {
        // 코스 추천에 쓰는 것과 같은 기록을 요약한다
        List<RunRecord> recentRecords = runRecordRepository.findRecentWithDistanceAndDuration(
                userId, PageRequest.of(0, CourseRecommendService.RECENT_RECORD_COUNT));

        // 오늘을 포함한 최근 7일
        LocalDate today = LocalDate.now(clock);
        BigDecimal weeklyDistance = runRecordRepository.sumDistanceBetween(
                userId, today.minusDays(WEEK_DAYS - 1).atStartOfDay(), today.plusDays(1).atStartOfDay());

        return RunnerSummaryRes.of(recentRecords, weeklyDistance);
    }
}
