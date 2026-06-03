package com._s3k.runsync.domain.artrun.dto.response;

import com._s3k.runsync.global.common.ScrollPaginationCollection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "협동 러닝 세션 목록 응답")
public class ArtRunScrollRes {

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private final boolean hasNext;

    @Schema(description = "다음 요청에 사용할 커서 (마지막 sessionId)", example = "90")
    private final Long nextCursor;

    @Schema(description = "협동 러닝 세션 목록")
    private final List<ArtRunRes> sessions;

    private ArtRunScrollRes(boolean hasNext, Long nextCursor, List<ArtRunRes> sessions) {
        this.hasNext = hasNext;
        this.nextCursor = nextCursor;
        this.sessions = sessions;
    }

    public static ArtRunScrollRes of(ScrollPaginationCollection<ArtRunRes> collection) {
        List<ArtRunRes> contents = collection.getCurrentPageContents();
        Long nextCursor = collection.hasNext() ? contents.get(contents.size() - 1).getSessionId() : null;
        return new ArtRunScrollRes(collection.hasNext(), nextCursor, contents);
    }
}
