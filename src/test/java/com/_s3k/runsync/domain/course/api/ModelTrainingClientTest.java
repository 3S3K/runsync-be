package com._s3k.runsync.domain.course.api;

import com._s3k.runsync.domain.course.dto.request.AiTrainingReq;
import com._s3k.runsync.domain.course.dto.response.AiTrainingRes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelTrainingClientTest {

    private static final String RESULT_JSON = """
            {"trainedAt": "2026-11-28T04:00:03+09:00", "receivedCount": 42, "usedCount": 37,
             "baselineMae": 3.1, "candidateMae": 2.8, "replaced": true, "reason": "검증 오차 개선"}
            """;

    private final AiTrainingReq request = AiTrainingReq.of(List.of(), LocalDate.of(2026, 11, 28));

    @Test
    @DisplayName("재학습 API를 내부 호출 키 헤더와 함께 호출하고 결과를 읽는다")
    void train_sendsInternalKey() {
        AtomicReference<ClientRequest> sent = new AtomicReference<>();
        ModelTrainingClient client = clientResponding(sent, HttpStatus.OK, RESULT_JSON, "secret-key");

        AiTrainingRes result = client.train(request);

        assertThat(sent.get().url().getPath()).isEqualTo("/model/trainings");
        assertThat(sent.get().headers().getFirst(ModelTrainingClient.INTERNAL_KEY_HEADER)).isEqualTo("secret-key");
        assertThat(result.getReceivedCount()).isEqualTo(42);
        assertThat(result.getUsedCount()).isEqualTo(37);
        assertThat(result.getBaselineMae()).isEqualTo(3.1);
        assertThat(result.getCandidateMae()).isEqualTo(2.8);
        assertThat(result.getReplaced()).isTrue();
    }

    @Test
    @DisplayName("추천 서버가 오류로 응답하면 예외가 발생한다")
    void train_serverError() {
        ModelTrainingClient client = clientResponding(new AtomicReference<>(), HttpStatus.UNAUTHORIZED,
                "{\"code\": \"INVALID_INTERNAL_KEY\"}", "wrong-key");

        assertThatThrownBy(() -> client.train(request)).isInstanceOf(WebClientResponseException.class);
    }

    @Test
    @DisplayName("내부 호출 키가 없거나 비어 있으면 재학습을 쓸 수 없다")
    void isEnabled() {
        assertThat(clientWithKey("secret-key").isEnabled()).isTrue();
        assertThat(clientWithKey("").isEnabled()).isFalse();
        assertThat(clientWithKey("  ").isEnabled()).isFalse();
        assertThat(clientWithKey(null).isEnabled()).isFalse();
    }

    private ModelTrainingClient clientWithKey(String key) {
        return new ModelTrainingClient(WebClient.builder().build(), key);
    }

    // 실제 서버 없이 정해진 응답을 돌려주고, 보낸 요청을 기록하는 WebClient
    private ModelTrainingClient clientResponding(AtomicReference<ClientRequest> sent, HttpStatus status,
                                                 String body, String key) {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(clientRequest -> {
                    sent.set(clientRequest);
                    return Mono.just(ClientResponse.create(status)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(body)
                            .build());
                })
                .build();
        return new ModelTrainingClient(webClient, key);
    }
}
