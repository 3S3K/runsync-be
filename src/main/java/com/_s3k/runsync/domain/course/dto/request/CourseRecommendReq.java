package com._s3k.runsync.domain.course.dto.request;

import com._s3k.runsync.entity.enums.CourseIntensity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "코스 추천 요청")
public class CourseRecommendReq {

    @NotNull(message = "출발 위도는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    @Schema(description = "출발 위도", example = "36.3321")
    private Double startLat;

    @NotNull(message = "출발 경도는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    @Schema(description = "출발 경도", example = "127.4562")
    private Double startLng;

    @NotNull(message = "목표 거리는 필수입니다.")
    @DecimalMin(value = "1.0", message = "목표 거리는 1km 이상이어야 합니다.")
    @DecimalMax(value = "20.0", message = "목표 거리는 20km 이하여야 합니다.")
    @Schema(description = "목표 거리(km)", example = "5.0")
    private Double targetDistanceKm;

    @NotNull(message = "강도는 필수입니다.")
    @Schema(description = "강도 (EASY: 가볍게, NORMAL: 보통, HARD: 힘들게)", example = "NORMAL")
    private CourseIntensity intensity;
}
