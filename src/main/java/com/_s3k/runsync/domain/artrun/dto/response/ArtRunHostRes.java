package com._s3k.runsync.domain.artrun.dto.response;

import com._s3k.runsync.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "호스트 정보")
public class ArtRunHostRes {

    @Schema(description = "호스트 사용자 ID", example = "1")
    private final Long userId;

    @Schema(description = "호스트 닉네임", example = "현우")
    private final String nickname;

    private ArtRunHostRes(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }

    public static ArtRunHostRes of(User host) {
        return new ArtRunHostRes(host.getId(), host.getNickname());
    }
}
