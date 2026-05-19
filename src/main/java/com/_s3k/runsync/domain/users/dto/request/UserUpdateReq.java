package com._s3k.runsync.domain.users.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "내 정보 수정 요청")
public class UserUpdateReq {
    @Schema(description = "변경할 닉네임", example = "run_hyunwoo")
    private String nickname;
    @Schema(description = "변경할 프로필 이미지 URL", example = "https://sample.com/new-profile.png")
    private String profileImage;
    @Schema(description = "성별", example = "MALE")
    private String gender;
    @Schema(description = "생년월일", example = "2001-01-12")
    private LocalDate birthDate;
}
