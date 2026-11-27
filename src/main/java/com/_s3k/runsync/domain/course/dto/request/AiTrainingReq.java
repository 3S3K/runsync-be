package com._s3k.runsync.domain.course.dto.request;

import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.User;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 추천 서버(runsync-ai)의 POST /model/trainings 요청 본문.
 * 어떤 기록을 학습에 쓸지는 추천 서버가 선별한다. 여기서는 걸러내지 않고 그대로 보낸다.
 */
@Getter
public class AiTrainingReq {

    private final List<TrainingRecord> records;

    private AiTrainingReq(List<TrainingRecord> records) {
        this.records = records;
    }

    public static AiTrainingReq of(List<RunRecord> runRecords, LocalDate today) {
        return new AiTrainingReq(runRecords.stream().map(record -> TrainingRecord.of(record, today)).toList());
    }

    @Getter
    public static class TrainingRecord {
        private final Long userId;
        private final Integer age;
        private final String gender;
        // 한국 시간의 ISO-8601 문자열 (시간대 표기 없음)
        private final String startTime;
        private final Double distanceKm;
        private final Integer durationSec;
        private final Integer avgHeartRate;
        private final Integer cadence;
        private final Double elevationGainM;

        private TrainingRecord(Long userId, Integer age, String gender, String startTime, Double distanceKm,
                               Integer durationSec, Integer avgHeartRate, Integer cadence, Double elevationGainM) {
            this.userId = userId;
            this.age = age;
            this.gender = gender;
            this.startTime = startTime;
            this.distanceKm = distanceKm;
            this.durationSec = durationSec;
            this.avgHeartRate = avgHeartRate;
            this.cadence = cadence;
            this.elevationGainM = elevationGainM;
        }

        private static TrainingRecord of(RunRecord record, LocalDate today) {
            User user = record.getUser();
            return new TrainingRecord(
                    user.getId(),
                    user.calculateAge(today),
                    user.getGender() != null ? user.getGender().name() : null,
                    record.getStartTime().toString(),
                    record.getDistance().doubleValue(),
                    record.getDurationSeconds(),
                    record.getAverageHeartRate(),
                    record.getCadence(),
                    record.getElevationGain() != null ? record.getElevationGain().doubleValue() : null
            );
        }
    }
}
