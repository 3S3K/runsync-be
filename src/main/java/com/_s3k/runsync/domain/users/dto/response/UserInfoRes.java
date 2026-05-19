package com._s3k.runsync.domain.users.dto.response;

import com._s3k.runsync.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Schema(description = "내 정보 조회 응답")
public class UserInfoRes {
    @Schema(description = "사용자 ID", example = "1")
    private Long id;
    @Schema(description = "닉네임", example = "김태경")
    private String nickname;
    @Schema(description = "소셜 로그인 제공자 ID", example = "kakao_123456")
    private String providerId;
    @Schema(description = "프로필 이미지 URL", example = "https://sample.com/profile.png")
    private String profileImage;
    @Schema(description = "사용자 권한", example = "USER")
    private String role;
    @Schema(description = "생년월일", example = "2001-01-12")
    private LocalDate birthDate;
    @Schema(description = "성별", example = "MALE")
    private String gender;

    private UserInfoRes(Long id, String nickname, String providerId, String profileImage,
                        String role, LocalDate birthDate, String gender) {
        this.id = id;
        this.nickname = nickname;
        this.providerId = providerId;
        this.profileImage = profileImage;
        this.role = role;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public static UserInfoRes of(User user) {
        return new UserInfoRes(
                user.getId(),
                user.getNickname(),
                user.getProviderId(),
                user.getProfileImage(),
                user.getRole().name(),
                user.getBirthDate(),
                user.getGender() != null ? user.getGender().name() : null
        );
    }
}
