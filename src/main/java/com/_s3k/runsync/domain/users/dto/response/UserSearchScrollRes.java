package com._s3k.runsync.domain.users.dto.response;

import com._s3k.runsync.global.common.ScrollPaginationCollection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "사용자 검색 목록 응답")
public class UserSearchScrollRes {

    @Schema(description = "다음 페이지 존재 여부", example = "false")
    private boolean hasNext;

    @Schema(description = "다음 요청에 사용할 커서 (마지막 userId)", example = "10")
    private Long nextCursor;

    @Schema(description = "검색된 사용자 목록")
    private List<UserSearchRes> users;

    private UserSearchScrollRes(boolean hasNext, Long nextCursor, List<UserSearchRes> users) {
        this.hasNext = hasNext;
        this.nextCursor = nextCursor;
        this.users = users;
    }

    public static UserSearchScrollRes of(ScrollPaginationCollection<UserSearchRes> collection) {
        List<UserSearchRes> contents = collection.getCurrentPageContents();
        Long nextCursor = collection.hasNext() ? contents.get(contents.size() - 1).getId() : null;
        return new UserSearchScrollRes(collection.hasNext(), nextCursor, contents);
    }
}
