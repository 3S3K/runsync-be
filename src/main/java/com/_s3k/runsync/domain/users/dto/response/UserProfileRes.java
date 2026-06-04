package com._s3k.runsync.domain.users.dto.response;

import com._s3k.runsync.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "특정 사용자 조회 응답")
public class UserProfileRes {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "닉네임", example = "run_hyunwoo")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://sample.com/new-profile.png")
    private String profileImage;

    private UserProfileRes(Long id, String nickname, String profileImage) {
        this.id = id;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public static UserProfileRes of(User user) {
        return new UserProfileRes(user.getId(), user.getNickname(), user.getProfileImage());
    }
}
