package com._s3k.runsync.domain.location.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "친구 위치 응답")
public class FriendLocationRes {

    @Schema(description = "친구 유저 ID", example = "2")
    private final Long friendId;

    @Schema(description = "위도", example = "37.5665")
    private final Double latitude;

    @Schema(description = "경도", example = "126.9780")
    private final Double longitude;

    private FriendLocationRes(Long friendId, Double latitude, Double longitude) {
        this.friendId = friendId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static FriendLocationRes of(Long friendId, Double latitude, Double longitude) {
        return new FriendLocationRes(friendId, latitude, longitude);
    }
}
