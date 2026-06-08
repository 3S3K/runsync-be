package com._s3k.runsync.domain.users.dto.response;

import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.UserRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "사용자 검색 결과")
public class UserSearchRes {

    @Schema(description = "사용자 ID", example = "10")
    private Long id;

    @Schema(description = "닉네임", example = "hw0o")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://sample.com/profile.png")
    private String profileImage;

    @Schema(description = "나와의 관계 (NONE / FRIEND / REQUEST_SENT / REQUEST_RECEIVED)", example = "NONE")
    private UserRelation relation;

    private UserSearchRes(Long id, String nickname, String profileImage, UserRelation relation) {
        this.id = id;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.relation = relation;
    }

    public static UserSearchRes of(User user, UserRelation relation) {
        return new UserSearchRes(user.getId(), user.getNickname(), user.getProfileImage(), relation);
    }
}
