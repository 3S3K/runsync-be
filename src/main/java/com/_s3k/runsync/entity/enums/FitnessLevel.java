package com._s3k.runsync.entity.enums;

/**
 * 최근 러닝의 평균 페이스로 나눈 체력 수준.
 */
public enum FitnessLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    // 5'30"/km보다 빠르면 상급, 7'00"/km보다 느리면 초급
    private static final int ADVANCED_PACE_LIMIT_SEC = 330;
    private static final int BEGINNER_PACE_LIMIT_SEC = 420;

    public static FitnessLevel fromPaceSecPerKm(int paceSecPerKm) {
        if (paceSecPerKm < ADVANCED_PACE_LIMIT_SEC) {
            return ADVANCED;
        }
        if (paceSecPerKm > BEGINNER_PACE_LIMIT_SEC) {
            return BEGINNER;
        }
        return INTERMEDIATE;
    }
}
