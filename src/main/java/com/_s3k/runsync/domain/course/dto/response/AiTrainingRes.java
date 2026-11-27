package com._s3k.runsync.domain.course.dto.response;

import lombok.Getter;

/**
 * AI 추천 서버(runsync-ai)의 POST /model/trainings 응답 본문.
 */
@Getter
public class AiTrainingRes {
    private String trainedAt;
    private Integer receivedCount;
    private Integer usedCount;
    // 합성 데이터만으로 학습한 모델의 오차(%p)
    private Double baselineMae;
    // 실제 기록을 함께 학습한 모델의 교차 검증 오차(%p)
    private Double candidateMae;
    private Boolean replaced;
    private String reason;
}
