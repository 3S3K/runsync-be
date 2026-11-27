package com._s3k.runsync.domain.course.service;

import com._s3k.runsync.domain.course.api.RecommendClient;
import com._s3k.runsync.domain.course.dto.request.AiRecommendReq;
import com._s3k.runsync.domain.course.dto.request.CourseRecommendReq;
import com._s3k.runsync.domain.course.dto.response.AiRecommendRes;
import com._s3k.runsync.domain.course.dto.response.CourseRecommendRes;
import com._s3k.runsync.domain.course.dto.response.CourseRes;
import com._s3k.runsync.domain.course.dto.response.CourseSlopeSegmentRes;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.CourseIntensity;
import com._s3k.runsync.entity.enums.Gender;
import com._s3k.runsync.global.exception.GlobalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourseRecommendServiceTest {

    private static final Long USER_ID = 12L;
    private static final String AI_RESPONSE_JSON = """
            {"courses": [{
              "rank": 1, "name": "용운로 순환", "score": 89,
              "distanceKm": 5.12, "elevationGainM": 38.0, "maxSlopePct": 6.4,
              "predictedHrPct": 74.5, "intensity": "NORMAL", "reason": "추천 이유",
              "geometry": {"type": "LineString",
                           "coordinates": [[127.4562, 36.3321], [127.4571, 36.3330], [127.4562, 36.3321]]},
              "elevationProfile": [{"distanceM": 0, "elevationM": 89.5}, {"distanceM": 150, "elevationM": 91.2}],
              "estimatedTimeMin": 31, "stairsCount": 2, "hrZone": "Z3", "hrBpmRange": [142, 152],
              "biggestClimb": {"startM": 2810, "lengthM": 450, "gainM": 11},
              "slopeSegments": [
                {"grade": "FLAT", "coordinates": [[127.4562, 36.3321], [127.4571, 36.3330]]},
                {"grade": "STEEP", "coordinates": [[127.4571, 36.3330], [127.4562, 36.3321]]}
              ]
            }]}
            """;
    // 상세 정보를 내려주지 않는 구버전 추천 서버의 응답
    private static final String LEGACY_AI_RESPONSE_JSON = """
            {"courses": [{
              "rank": 1, "distanceKm": 5.12, "elevationGainM": 38.0, "maxSlopePct": 6.4,
              "predictedHrPct": 74.5, "intensity": "NORMAL", "reason": "추천 이유",
              "geometry": {"type": "LineString",
                           "coordinates": [[127.4562, 36.3321], [127.4571, 36.3330], [127.4562, 36.3321]]}
            }]}
            """;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RunRecordRepository runRecordRepository;

    @Mock
    private RecommendClient recommendClient;

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 27);

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-27T00:00:00Z"), ZoneOffset.UTC);

    private CourseRecommendService courseRecommendService;

    @BeforeEach
    void setUp() {
        courseRecommendService = new CourseRecommendService(userRepository, runRecordRepository, recommendClient, clock);
    }

    @Test
    @DisplayName("사용자 정보와 최근 기록을 추천 서버 요청으로 변환하고 응답을 그대로 돌려준다")
    void createRecommendation_success() throws Exception {
        // given
        givenUser(24, Gender.MALE);
        RunRecord record = record("5.20", 1890);
        given(record.getAverageHeartRate()).willReturn(158);
        given(record.getCadence()).willReturn(168);
        given(record.getElevationGain()).willReturn(new BigDecimal("22.00"));
        givenRecords(List.of(record));
        given(recommendClient.recommend(any())).willReturn(aiResponse());

        // when
        CourseRecommendRes result = courseRecommendService.createRecommendation(USER_ID, request());

        // then
        AiRecommendReq sent = captureAiRequest();
        assertThat(sent.getStart().getLat()).isEqualTo(36.3321);
        assertThat(sent.getStart().getLng()).isEqualTo(127.4562);
        assertThat(sent.getTargetDistanceKm()).isEqualTo(5.0);
        assertThat(sent.getIntensity()).isEqualTo(CourseIntensity.NORMAL);
        assertThat(sent.getUser().getUserId()).isEqualTo(USER_ID);
        assertThat(sent.getUser().getAge()).isEqualTo(24);
        assertThat(sent.getUser().getGender()).isEqualTo("MALE");
        assertThat(sent.getRecentRecords()).hasSize(1);
        AiRecommendReq.RecentRecord sentRecord = sent.getRecentRecords().get(0);
        assertThat(sentRecord.getDistanceKm()).isEqualTo(5.2);
        assertThat(sentRecord.getDurationSec()).isEqualTo(1890);
        assertThat(sentRecord.getAvgHeartRate()).isEqualTo(158);
        assertThat(sentRecord.getCadence()).isEqualTo(168);
        assertThat(sentRecord.getElevationGainM()).isEqualTo(22.0);

        assertThat(result.getCourses()).hasSize(1);
        CourseRes course = result.getCourses().get(0);
        assertThat(course.getRank()).isEqualTo(1);
        assertThat(course.getDistanceKm()).isEqualTo(5.12);
        assertThat(course.getElevationGainM()).isEqualTo(38.0);
        assertThat(course.getMaxSlopePct()).isEqualTo(6.4);
        assertThat(course.getPredictedHrPct()).isEqualTo(74.5);
        assertThat(course.getIntensity()).isEqualTo(CourseIntensity.NORMAL);
        assertThat(course.getReason()).isEqualTo("추천 이유");
        assertThat(course.getGeometry().getType()).isEqualTo("LineString");
        assertThat(course.getGeometry().getCoordinates()).hasSize(3);
        assertThat(course.getGeometry().getCoordinates().get(0)).containsExactly(127.4562, 36.3321);
    }

    @Test
    @DisplayName("추천 서버가 내려준 코스 상세 정보를 응답에 담는다")
    void createRecommendation_passesCourseDetail() throws Exception {
        // given
        givenUser(24, Gender.MALE);
        givenRecords(List.of());
        given(recommendClient.recommend(any())).willReturn(aiResponse());

        // when
        CourseRecommendRes result = courseRecommendService.createRecommendation(USER_ID, request());

        // then
        CourseRes course = result.getCourses().get(0);
        assertThat(course.getName()).isEqualTo("용운로 순환");
        assertThat(course.getScore()).isEqualTo(89);
        assertThat(course.getEstimatedTimeMin()).isEqualTo(31);
        assertThat(course.getStairsCount()).isEqualTo(2);
        assertThat(course.getHrZone()).isEqualTo("Z3");
        assertThat(course.getHrBpmRange()).containsExactly(142, 152);
        assertThat(course.getElevationProfile()).hasSize(2);
        assertThat(course.getElevationProfile().get(1).getDistanceM()).isEqualTo(150);
        assertThat(course.getElevationProfile().get(1).getElevationM()).isEqualTo(91.2);
        assertThat(course.getBiggestClimb().getStartM()).isEqualTo(2810);
        assertThat(course.getBiggestClimb().getLengthM()).isEqualTo(450);
        assertThat(course.getBiggestClimb().getGainM()).isEqualTo(11);
        assertThat(course.getSlopeSegments())
                .extracting(CourseSlopeSegmentRes::getGrade)
                .containsExactly("FLAT", "STEEP");
        assertThat(course.getSlopeSegments().get(0).getCoordinates().get(0)).containsExactly(127.4562, 36.3321);
    }

    @Test
    @DisplayName("추천 서버가 상세 정보를 내려주지 않으면 목록은 빈 목록, 나머지는 null로 돌려준다")
    void createRecommendation_withoutCourseDetail() throws Exception {
        // given
        givenUser(24, Gender.MALE);
        givenRecords(List.of());
        given(recommendClient.recommend(any()))
                .willReturn(new ObjectMapper().readValue(LEGACY_AI_RESPONSE_JSON, AiRecommendRes.class));

        // when
        CourseRecommendRes result = courseRecommendService.createRecommendation(USER_ID, request());

        // then
        CourseRes course = result.getCourses().get(0);
        assertThat(course.getDistanceKm()).isEqualTo(5.12);
        assertThat(course.getElevationProfile()).isEmpty();
        assertThat(course.getSlopeSegments()).isEmpty();
        assertThat(course.getName()).isNull();
        assertThat(course.getScore()).isNull();
        assertThat(course.getEstimatedTimeMin()).isNull();
        assertThat(course.getHrBpmRange()).isNull();
        assertThat(course.getBiggestClimb()).isNull();
    }

    @Test
    @DisplayName("나이·성별·기록이 없으면 null과 빈 목록으로 보낸다")
    void createRecommendation_withoutProfileAndRecords() throws Exception {
        // given
        givenUser(null, null);
        givenRecords(List.of());
        given(recommendClient.recommend(any())).willReturn(aiResponse());

        // when
        courseRecommendService.createRecommendation(USER_ID, request());

        // then
        AiRecommendReq sent = captureAiRequest();
        assertThat(sent.getUser().getAge()).isNull();
        assertThat(sent.getUser().getGender()).isNull();
        assertThat(sent.getRecentRecords()).isEmpty();
    }

    @Test
    @DisplayName("거리와 시간이 있는 기록을 최근 5개만 조회해 순서대로 보낸다")
    void createRecommendation_requestsFiveRecentRecords() throws Exception {
        // given
        givenUser(24, Gender.MALE);
        List<RunRecord> records = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> record(i + ".00", 1000 + i))
                .toList();
        givenRecords(records);
        given(recommendClient.recommend(any())).willReturn(aiResponse());

        // when
        courseRecommendService.createRecommendation(USER_ID, request());

        // then
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(runRecordRepository).findRecentWithDistanceAndDuration(eq(USER_ID), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);

        AiRecommendReq sent = captureAiRequest();
        assertThat(sent.getRecentRecords())
                .extracting(AiRecommendReq.RecentRecord::getDistanceKm)
                .containsExactly(1.0, 2.0, 3.0, 4.0, 5.0);
    }

    @Test
    @DisplayName("사용자가 없으면 예외가 발생하고 추천 서버를 호출하지 않는다")
    void createRecommendation_userNotFound() {
        // given
        given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> courseRecommendService.createRecommendation(USER_ID, mock(CourseRecommendReq.class)))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
        verify(recommendClient, never()).recommend(any());
    }

    // 나이 계산 자체는 UserTest에서 검증한다. 여기서는 오늘 날짜로 물어보는지만 확인한다
    private void givenUser(Integer age, Gender gender) {
        User user = mock(User.class);
        given(user.getId()).willReturn(USER_ID);
        given(user.calculateAge(TODAY)).willReturn(age);
        given(user.getGender()).willReturn(gender);
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
    }

    private void givenRecords(List<RunRecord> records) {
        given(runRecordRepository.findRecentWithDistanceAndDuration(eq(USER_ID), any(Pageable.class))).willReturn(records);
    }

    private RunRecord record(String distanceKm, int durationSeconds) {
        RunRecord record = mock(RunRecord.class);
        given(record.getDistance()).willReturn(new BigDecimal(distanceKm));
        given(record.getDurationSeconds()).willReturn(durationSeconds);
        return record;
    }

    private CourseRecommendReq request() {
        CourseRecommendReq request = mock(CourseRecommendReq.class);
        given(request.getStartLat()).willReturn(36.3321);
        given(request.getStartLng()).willReturn(127.4562);
        given(request.getTargetDistanceKm()).willReturn(5.0);
        given(request.getIntensity()).willReturn(CourseIntensity.NORMAL);
        return request;
    }

    private AiRecommendRes aiResponse() throws Exception {
        return new ObjectMapper().readValue(AI_RESPONSE_JSON, AiRecommendRes.class);
    }

    private AiRecommendReq captureAiRequest() {
        ArgumentCaptor<AiRecommendReq> captor = ArgumentCaptor.forClass(AiRecommendReq.class);
        verify(recommendClient).recommend(captor.capture());
        return captor.getValue();
    }
}
