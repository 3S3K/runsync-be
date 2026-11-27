package com._s3k.runsync.domain.course.api;

import com._s3k.runsync.domain.course.dto.request.AiTrainingReq;
import com._s3k.runsync.domain.course.dto.response.AiTrainingRes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * AI 추천 서버의 심박 예측 모델 재학습 API 호출.
 */
@Component
public class ModelTrainingClient {

    static final String INTERNAL_KEY_HEADER = "X-Internal-Api-Key";
    // 재학습은 모델을 여러 번 학습해 검증하므로 추천 호출보다 오래 걸린다
    private static final Duration TIMEOUT = Duration.ofMinutes(3);

    private final WebClient aiServerWebClient;
    private final String internalApiKey;

    public ModelTrainingClient(WebClient aiServerWebClient,
                               @Value("${ai.internal-api-key:}") String internalApiKey) {
        this.aiServerWebClient = aiServerWebClient;
        this.internalApiKey = internalApiKey;
    }

    // 추천 서버와 약속한 키가 설정돼 있어야 재학습을 호출할 수 있다 (환경변수 AI_INTERNAL_API_KEY)
    public boolean isEnabled() {
        return internalApiKey != null && !internalApiKey.isBlank();
    }

    public AiTrainingRes train(AiTrainingReq request) {
        return aiServerWebClient.post()
                .uri("/model/trainings")
                .header(INTERNAL_KEY_HEADER, internalApiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiTrainingRes.class)
                .block(TIMEOUT);
    }
}
