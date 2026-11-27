package com._s3k.runsync.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "고도 프로필의 한 지점")
public class CourseElevationPointRes {

    @Schema(description = "출발점부터의 거리(m)", example = "150")
    private final Integer distanceM;

    @Schema(description = "해발 고도(m)", example = "89.5")
    private final Double elevationM;

    private CourseElevationPointRes(Integer distanceM, Double elevationM) {
        this.distanceM = distanceM;
        this.elevationM = elevationM;
    }

    public static CourseElevationPointRes of(AiRecommendRes.ElevationPoint point) {
        return new CourseElevationPointRes(point.getDistanceM(), point.getElevationM());
    }
}
