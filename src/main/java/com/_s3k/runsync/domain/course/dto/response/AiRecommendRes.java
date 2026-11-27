package com._s3k.runsync.domain.course.dto.response;

import com._s3k.runsync.entity.enums.CourseIntensity;
import lombok.Getter;

import java.util.List;

/**
 * AI 추천 서버(runsync-ai)의 POST /recommend 응답 본문.
 */
@Getter
public class AiRecommendRes {

    private List<Course> courses;

    @Getter
    public static class Course {
        private Integer rank;
        private String name;
        private Integer score;
        private Double distanceKm;
        private Double elevationGainM;
        private Double maxSlopePct;
        private Double predictedHrPct;
        private CourseIntensity intensity;
        private String reason;
        private Geometry geometry;
        private List<ElevationPoint> elevationProfile;
        private Integer estimatedTimeMin;
        private Integer stairsCount;
        private String hrZone;
        private List<Integer> hrBpmRange;
        private Climb biggestClimb;
        private List<SlopeSegment> slopeSegments;
    }

    @Getter
    public static class Geometry {
        private String type;
        private List<List<Double>> coordinates;
    }

    @Getter
    public static class ElevationPoint {
        private Integer distanceM;
        private Double elevationM;
    }

    @Getter
    public static class Climb {
        private Integer startM;
        private Integer lengthM;
        private Integer gainM;
    }

    @Getter
    public static class SlopeSegment {
        private String grade;
        private List<List<Double>> coordinates;
    }
}
