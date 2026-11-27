package com._s3k.runsync.domain.course.dto.request;

import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.CourseIntensity;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 추천 서버(runsync-ai)의 POST /recommend 요청 본문.
 * 추천 서버는 DB를 직접 읽지 않으므로 사용자 정보와 최근 기록을 여기에 담아 보낸다.
 */
@Getter
public class AiRecommendReq {

    private final Start start;
    private final Double targetDistanceKm;
    private final CourseIntensity intensity;
    private final UserInfo user;
    private final List<RecentRecord> recentRecords;

    private AiRecommendReq(Start start, Double targetDistanceKm, CourseIntensity intensity,
                           UserInfo user, List<RecentRecord> recentRecords) {
        this.start = start;
        this.targetDistanceKm = targetDistanceKm;
        this.intensity = intensity;
        this.user = user;
        this.recentRecords = recentRecords;
    }

    public static AiRecommendReq of(CourseRecommendReq request, User user, LocalDate today,
                                    List<RunRecord> recentRecords) {
        return new AiRecommendReq(
                new Start(request.getStartLat(), request.getStartLng()),
                request.getTargetDistanceKm(),
                request.getIntensity(),
                UserInfo.of(user, today),
                recentRecords.stream().map(RecentRecord::of).toList()
        );
    }

    @Getter
    public static class Start {
        private final Double lat;
        private final Double lng;

        private Start(Double lat, Double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    @Getter
    public static class UserInfo {
        private final Long userId;
        private final Integer age;
        private final String gender;

        private UserInfo(Long userId, Integer age, String gender) {
            this.userId = userId;
            this.age = age;
            this.gender = gender;
        }

        private static UserInfo of(User user, LocalDate today) {
            return new UserInfo(
                    user.getId(),
                    user.calculateAge(today),
                    user.getGender() != null ? user.getGender().name() : null
            );
        }
    }

    @Getter
    public static class RecentRecord {
        private final Double distanceKm;
        private final Integer durationSec;
        private final Integer avgHeartRate;
        private final Integer cadence;
        private final Double elevationGainM;

        private RecentRecord(Double distanceKm, Integer durationSec, Integer avgHeartRate,
                             Integer cadence, Double elevationGainM) {
            this.distanceKm = distanceKm;
            this.durationSec = durationSec;
            this.avgHeartRate = avgHeartRate;
            this.cadence = cadence;
            this.elevationGainM = elevationGainM;
        }

        private static RecentRecord of(RunRecord record) {
            return new RecentRecord(
                    record.getDistance().doubleValue(),
                    record.getDurationSeconds(),
                    record.getAverageHeartRate(),
                    record.getCadence(),
                    record.getElevationGain() != null ? record.getElevationGain().doubleValue() : null
            );
        }
    }
}
