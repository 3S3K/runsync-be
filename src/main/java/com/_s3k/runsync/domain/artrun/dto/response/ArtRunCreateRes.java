package com._s3k.runsync.domain.artrun.dto.response;

import com._s3k.runsync.entity.ArtRunSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "협동 러닝 세션 생성 응답")
public class ArtRunCreateRes {

    @Schema(description = "생성된 세션 ID", example = "100")
    private final Long sessionId;

    private ArtRunCreateRes(Long sessionId) {
        this.sessionId = sessionId;
    }

    public static ArtRunCreateRes of(ArtRunSession session) {
        return new ArtRunCreateRes(session.getId());
    }
}
