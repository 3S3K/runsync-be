package com._s3k.runsync.domain.users.dto.response;

import com._s3k.runsync.global.common.ScrollPaginationCollection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "러닝 기록 목록 응답")
public class UserRecordsScrollRes {

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;
    @Schema(description = "다음 요청에 사용할 커서 (마지막 recordId)", example = "3")
    private Long nextCursor;
    @Schema(description = "러닝 기록 목록")
    private List<RecordRes> records;

    private UserRecordsScrollRes(boolean hasNext, Long nextCursor, List<RecordRes> records) {
        this.hasNext = hasNext;
        this.nextCursor = nextCursor;
        this.records = records;
    }

    public static UserRecordsScrollRes of(ScrollPaginationCollection<RecordRes> collection) {
        List<RecordRes> contents = collection.getCurrentPageContents();
        Long nextCursor = collection.hasNext() ? contents.get(contents.size() - 1).getRecordId() : null;
        return new UserRecordsScrollRes(collection.hasNext(), nextCursor, contents);
    }
}
