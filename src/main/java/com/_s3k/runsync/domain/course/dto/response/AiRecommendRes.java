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
        private Double distanceKm;
        private Double elevationGainM;
        private Double maxSlopePct;
        private Double predictedHrPct;
        private CourseIntensity intensity;
        private String reason;
        private Geometry geometry;
    }

    @Getter
    public static class Geometry {
        private String type;
        private List<List<Double>> coordinates;
    }
}
