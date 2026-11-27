package com._s3k.runsync.domain.course.api;

import com._s3k.runsync.domain.course.dto.request.AiRecommendReq;
import com._s3k.runsync.domain.course.dto.response.AiRecommendRes;
import com._s3k.runsync.domain.course.exception.CourseErrorCode;
import com._s3k.runsync.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class RecommendClientTest {

    private static final String COURSES_JSON = """
            {"courses": [{
              "rank": 1, "distanceKm": 5.12, "elevationGainM": 38.0, "maxSlopePct": 6.4,
              "predictedHrPct": 74.5, "intensity": "NORMAL", "reason": "추천 이유",
              "geometry": {"type": "LineString", "coordinates": [[127.4562, 36.3321], [127.4562, 36.3321]]}
            }]}
            """;

    private final AiRecommendReq request = mock(AiRecommendReq.class);

    @Test
    @DisplayName("추천 서버의 200 응답을 코스 목록으로 읽는다")
    void recommend_success() {
        RecommendClient client = clientResponding(HttpStatus.OK, COURSES_JSON);

        AiRecommendRes response = client.recommend(request);

        assertThat(response.getCourses()).hasSize(1);
        assertThat(response.getCourses().get(0).getDistanceKm()).isEqualTo(5.12);
        assertThat(response.getCourses().get(0).getPredictedHrPct()).isEqualTo(74.5);
        assertThat(response.getCourses().get(0).getGeometry().getCoordinates()).hasSize(2);
    }

    @Test
    @DisplayName("추천 서버의 400은 서비스 지역 밖 에러로 바꾼다")
    void recommend_outOfServiceArea() {
        RecommendClient client = clientResponding(HttpStatus.BAD_REQUEST,
                "{\"code\": \"OUT_OF_SERVICE_AREA\", \"message\": \"서비스 지역이 아닙니다.\"}");

        assertErrorCode(client, CourseErrorCode.OUT_OF_SERVICE_AREA);
    }

    @Test
    @DisplayName("추천 서버의 404는 코스 없음 에러로 바꾼다")
    void recommend_courseNotFound() {
        RecommendClient client = clientResponding(HttpStatus.NOT_FOUND,
                "{\"code\": \"COURSE_NOT_FOUND\", \"message\": \"코스를 만들지 못했어요.\"}");

        assertErrorCode(client, CourseErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("추천 서버의 5xx와 요청 형식 오류(422)는 추천 서버 에러로 바꾼다")
    void recommend_serverError() {
        assertErrorCode(clientResponding(HttpStatus.INTERNAL_SERVER_ERROR, "{}"), CourseErrorCode.RECOMMEND_SERVER_ERROR);
        assertErrorCode(clientResponding(HttpStatus.UNPROCESSABLE_ENTITY, "{\"detail\": []}"), CourseErrorCode.RECOMMEND_SERVER_ERROR);
    }

    @Test
    @DisplayName("추천 서버에 연결하지 못하면 추천 서버 에러로 바꾼다")
    void recommend_connectionFailed() {
        RecommendClient client = clientWith(clientRequest -> Mono.error(new IllegalStateException("connection refused")));

        assertErrorCode(client, CourseErrorCode.RECOMMEND_SERVER_ERROR);
    }

    @Test
    @DisplayName("응답 본문에 코스 목록이 없으면 추천 서버 에러로 바꾼다")
    void recommend_emptyBody() {
        assertErrorCode(clientResponding(HttpStatus.OK, "{}"), CourseErrorCode.RECOMMEND_SERVER_ERROR);
    }

    private void assertErrorCode(RecommendClient client, CourseErrorCode expected) {
        assertThatThrownBy(() -> client.recommend(request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode()).isEqualTo(expected));
    }

    // 실제 서버 없이 정해진 응답을 돌려주는 WebClient
    private RecommendClient clientResponding(HttpStatus status, String body) {
        return clientWith(clientRequest -> Mono.just(
                ClientResponse.create(status)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(body)
                        .build()));
    }

    private RecommendClient clientWith(ExchangeFunction exchangeFunction) {
        return new RecommendClient(WebClient.builder().exchangeFunction(exchangeFunction).build());
    }
}
