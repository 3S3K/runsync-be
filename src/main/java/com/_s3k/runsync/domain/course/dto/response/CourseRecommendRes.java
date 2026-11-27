package com._s3k.runsync.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "코스 추천 응답")
public class CourseRecommendRes {

    @Schema(description = "추천 코스 목록 (최대 3개, 순위 순)")
    private final List<CourseRes> courses;

    private CourseRecommendRes(List<CourseRes> courses) {
        this.courses = courses;
    }

    public static CourseRecommendRes of(AiRecommendRes response) {
        return new CourseRecommendRes(response.getCourses().stream().map(CourseRes::of).toList());
    }
}
