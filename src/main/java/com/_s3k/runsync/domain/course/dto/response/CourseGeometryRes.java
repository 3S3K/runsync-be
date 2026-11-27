package com._s3k.runsync.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "코스 경로 (GeoJSON LineString)")
public class CourseGeometryRes {

    @Schema(description = "GeoJSON 타입", example = "LineString")
    private final String type;

    @Schema(description = "[경도, 위도] 순서의 좌표 배열. 첫 점과 끝 점이 같다(순환)",
            example = "[[127.4562, 36.3321], [127.4571, 36.3330], [127.4562, 36.3321]]")
    private final List<List<Double>> coordinates;

    private CourseGeometryRes(String type, List<List<Double>> coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public static CourseGeometryRes of(AiRecommendRes.Geometry geometry) {
        return new CourseGeometryRes(geometry.getType(), geometry.getCoordinates());
    }
}
