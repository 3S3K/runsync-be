package com._s3k.runsync.domain.users.dto.response;

import com._s3k.runsync.entity.User;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class UserInfoResponse {
    private Long id;
    private String nickname;
    private String profileImage;
    private LocalDate birthDate;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .birthDate(user.getBirthDate())
                .build();
    }
}
