package com._s3k.runsync.domain.users.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {
    private String nickname;      // 바꾸고 싶은 닉네임
    private String profileImage;  // 바꾸고 싶은 프로필 이미지
}
