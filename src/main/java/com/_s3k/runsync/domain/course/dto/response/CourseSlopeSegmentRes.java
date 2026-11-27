package com._s3k.runsync.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "경사 등급이 같은 경로 구간 (지도에 경사별 색을 칠할 때 사용)")
public class CourseSlopeSegmentRes {

    @Schema(description = "경사 등급. FLAT(3% 미만) | MODERATE(3~6%) | STEEP(6% 이상)", example = "FLAT")
    private final String grade;

    @Schema(description = "[경도, 위도] 순서의 좌표 배열",
            example = "[[127.4562, 36.3321], [127.4571, 36.3330]]")
    private final List<List<Double>> coordinates;

    private CourseSlopeSegmentRes(String grade, List<List<Double>> coordinates) {
        this.grade = grade;
        this.coordinates = coordinates;
    }

    public static CourseSlopeSegmentRes of(AiRecommendRes.SlopeSegment segment) {
        return new CourseSlopeSegmentRes(segment.getGrade(), segment.getCoordinates());
    }
}
