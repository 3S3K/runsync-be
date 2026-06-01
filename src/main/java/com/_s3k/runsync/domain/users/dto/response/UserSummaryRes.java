package com._s3k.runsync.domain.users.dto.response;

import com._s3k.runsync.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "개인페이지 프로필 + 월간 통계 응답")
public class UserSummaryRes {

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;
    @Schema(description = "프로필 이미지 URL", example = "https://sample.com/profile.png")
    private String profileImage;
    @Schema(description = "이번 달 통계")
    private MonthlyStats monthlyStats;

    @Getter
    @Schema(description = "월간 러닝 통계")
    public static class MonthlyStats {

        @Schema(description = "이번 달 총 거리 (km)", example = "25.30")
        private Double totalDistance;
        @Schema(description = "이번 달 러닝 횟수", example = "5")
        private Integer totalRunCount;

        private MonthlyStats(Double totalDistance, Integer totalRunCount) {
            this.totalDistance = totalDistance;
            this.totalRunCount = totalRunCount;
        }

        public static MonthlyStats of(Double totalDistance, Integer totalRunCount) {
            return new MonthlyStats(totalDistance, totalRunCount);
        }
    }

    private UserSummaryRes(String nickname, String profileImage, MonthlyStats monthlyStats) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.monthlyStats = monthlyStats;
    }

    public static UserSummaryRes of(User user, Double totalDistance, Integer totalRunCount) {
        return new UserSummaryRes(
                user.getNickname(),
                user.getProfileImage(),
                MonthlyStats.of(totalDistance, totalRunCount)
        );
    }
}
