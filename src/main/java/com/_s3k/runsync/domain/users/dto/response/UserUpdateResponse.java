package com._s3k.runsync.domain.users.dto.response;

import com._s3k.runsync.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateResponse {
    private Long id;
    private String nickname;
    private String profileImage;

    public static UserUpdateResponse from(User user) {
        return UserUpdateResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .build();
    }
}

