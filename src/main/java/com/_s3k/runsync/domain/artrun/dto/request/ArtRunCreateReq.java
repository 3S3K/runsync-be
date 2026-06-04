package com._s3k.runsync.domain.artrun.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "협동 러닝 세션 생성 요청")
public class ArtRunCreateReq {

    @NotBlank
    @Schema(description = "모집 제목", example = "강아지런 같이 그려요")
    private String title;

    @NotNull
    @Min(2)
    @Schema(description = "정원 (호스트 포함, 최소 2명)", example = "5")
    private Integer capacity;

    @NotNull
    @FutureOrPresent
    @Schema(description = "모이는 시간", example = "2026-06-10T07:00:00")
    private LocalDateTime meetingTime;

    @NotNull
    @Valid
    @Schema(description = "모이는 장소")
    private MeetingPlaceReq meetingPlace;

    @NotNull
    @Size(min = 2)
    @Schema(description = "도안 좌표 (그리는 순서, 2개 이상)")
    private List<@NotNull @Valid CoordinateReq> coordinates;
}
