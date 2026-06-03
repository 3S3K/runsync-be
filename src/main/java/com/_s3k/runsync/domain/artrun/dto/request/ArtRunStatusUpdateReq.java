package com._s3k.runsync.domain.artrun.dto.request;

import com._s3k.runsync.entity.enums.ArtRunStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "협동 러닝 세션 상태 변경 요청")
public class ArtRunStatusUpdateReq {

    @NotNull
    @Schema(description = "변경할 상태 (IN_PROGRESS: 시작, COMPLETED: 종료)", example = "IN_PROGRESS")
    private ArtRunStatus status;
}
