package com._s3k.runsync.domain.course.service;

import com._s3k.runsync.domain.course.api.ModelTrainingClient;
import com._s3k.runsync.domain.course.dto.request.AiTrainingReq;
import com._s3k.runsync.domain.course.dto.response.AiTrainingRes;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.entity.RunRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * 쌓인 러닝 기록으로 심박 예측 모델을 주기적으로 재학습시킨다.
 * 추천 서버는 회원·기록 데이터를 직접 읽지 않으므로, 백엔드가 기록을 모아 전달한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelTrainingService {

    // 추천 서버가 한 번에 받는 기록 수의 상한과 같다. 넘으면 최근 기록부터 보낸다
    static final int MAX_TRAINING_RECORDS = 50_000;

    private final RunRecordRepository runRecordRepository;
    private final ModelTrainingClient modelTrainingClient;
    private final Clock clock;

    // 기본은 매일 새벽 4시. 확인이 필요할 때는 환경변수 AI_TRAINING_CRON 으로 실행 시각을 바꾼다.
    // 서버가 여러 대가 되면 서버마다 실행되므로 그때는 한 대만 실행하도록 잠금이 필요하다
    @Scheduled(cron = "${ai.training-cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void runScheduledTraining() {
        try {
            createTraining();
        } catch (RuntimeException e) {
            // 재학습 실패가 다른 기능에 영향을 주지 않게 한다. 서비스 중인 모델은 그대로다
            log.error("Model training failed", e);
        }
    }

    // 외부 서버 호출 동안 DB 커넥션을 잡고 있지 않도록 메서드 전체를 트랜잭션으로 묶지 않는다.
    public void createTraining() {
        if (!modelTrainingClient.isEnabled()) {
            log.info("Model training skipped: internal api key is not configured");
            return;
        }

        List<RunRecord> records = runRecordRepository
                .findRecentForTraining(PageRequest.of(0, MAX_TRAINING_RECORDS));
        if (records.isEmpty()) {
            log.info("Model training skipped: no run records");
            return;
        }

        AiTrainingRes result = modelTrainingClient.train(AiTrainingReq.of(records, LocalDate.now(clock)));
        if (result == null) {
            log.warn("Model training returned an empty body");
            return;
        }
        log.info("Model training finished: received={}, used={}, baselineMae={}, candidateMae={}, replaced={}, reason={}",
                result.getReceivedCount(), result.getUsedCount(), result.getBaselineMae(),
                result.getCandidateMae(), result.getReplaced(), result.getReason());
    }
}
