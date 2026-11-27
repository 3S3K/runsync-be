package com._s3k.runsync.domain.course.api;

import com._s3k.runsync.domain.course.dto.request.AiRecommendReq;
import com._s3k.runsync.domain.course.dto.response.AiRecommendRes;
import com._s3k.runsync.domain.course.exception.CourseErrorCode;
import com._s3k.runsync.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebClient aiServerWebClient;

    public AiRecommendRes recommend(AiRecommendReq request) {
        AiRecommendRes response;
        try {
            response = aiServerWebClient.post()
                    .uri("/recommend")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiRecommendRes.class)
                    .block(TIMEOUT);
        } catch (WebClientResponseException e) {
            throw toGlobalException(e);
        } catch (RuntimeException e) {
            // 연결 실패, 타임아웃 등 응답을 받지 못한 경우
            log.error("AI recommend server call failed", e);
            throw new GlobalException(CourseErrorCode.RECOMMEND_SERVER_ERROR);
        }

        if (response == null || response.getCourses() == null) {
            log.error("AI recommend server returned an empty body");
            throw new GlobalException(CourseErrorCode.RECOMMEND_SERVER_ERROR);
        }
        return response;
    }

    // 추천 서버의 400은 서비스 지역 밖, 404는 조건에 맞는 코스 없음이다.
    // 그 밖의 응답(422 요청 형식 오류, 5xx)은 사용자가 고칠 수 없는 서버 쪽 문제로 본다.
    private GlobalException toGlobalException(WebClientResponseException e) {
        int status = e.getStatusCode().value();
        if (status == 400) {
            return new GlobalException(CourseErrorCode.OUT_OF_SERVICE_AREA);
        }
        if (status == 404) {
            return new GlobalException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        log.error("AI recommend server responded with status={}, body={}", status, e.getResponseBodyAsString());
        return new GlobalException(CourseErrorCode.RECOMMEND_SERVER_ERROR);
    }
}
