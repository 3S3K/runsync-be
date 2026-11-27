package com._s3k.runsync.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "코스에서 가장 많이 오르는 구간")
public class CourseClimbRes {

    @Schema(description = "출발점부터 오르막이 시작하는 지점까지의 거리(m)", example = "2810")
    private final Integer startM;

    @Schema(description = "오르막의 길이(m)", example = "450")
    private final Integer lengthM;

    @Schema(description = "오르는 높이(m)", example = "11")
    private final Integer gainM;

    private CourseClimbRes(Integer startM, Integer lengthM, Integer gainM) {
        this.startM = startM;
        this.lengthM = lengthM;
        this.gainM = gainM;
    }

    public static CourseClimbRes of(AiRecommendRes.Climb climb) {
        return new CourseClimbRes(climb.getStartM(), climb.getLengthM(), climb.getGainM());
    }
}
