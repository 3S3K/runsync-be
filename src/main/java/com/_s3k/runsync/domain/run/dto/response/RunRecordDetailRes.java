package com._s3k.runsync.domain.run.dto.response;

import com._s3k.runsync.entity.RunRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "러닝 기록 세부 입력 응답")
public class RunRecordDetailRes {

    @Schema(description = "러닝 기록 ID", example = "101")
    private final Long recordId;

    private RunRecordDetailRes(Long recordId) {
        this.recordId = recordId;
    }

    public static RunRecordDetailRes of(RunRecord record) {
        return new RunRecordDetailRes(record.getId());
    }
}
