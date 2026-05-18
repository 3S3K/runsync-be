package com._s3k.runsync.domain.run.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Schema(description = "러닝 기록 세부 입력 요청")
public class RunRecordDetailReq {

    @PositiveOrZero
    @Schema(description = "평균 페이스(min/km)", example = "6.43")
    private Double averagePace;

    @PositiveOrZero
    @Schema(description = "소모 칼로리(kcal)", example = "450")
    private Integer calories;

    @PositiveOrZero
    @Schema(description = "평균 심박수(bpm)", example = "172")
    private Integer averageHeartRate;

    @PositiveOrZero
    @Schema(description = "케이던스(spm)", example = "167")
    private Integer cadence;

    @PositiveOrZero
    @Schema(description = "고도 상승(m)", example = "5.0")
    private Double elevationGain;

    public BigDecimal getAveragePaceAsBigDecimal() {
        return averagePace != null ? BigDecimal.valueOf(averagePace) : null;
    }

    public BigDecimal getElevationGainAsBigDecimal() {
        return elevationGain != null ? BigDecimal.valueOf(elevationGain) : null;
    }
}
