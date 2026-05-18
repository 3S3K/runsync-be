package com._s3k.runsync.domain.run.service;

import com._s3k.runsync.domain.location.service.LocationService;
import com._s3k.runsync.domain.run.dto.request.LocationUpdateReq;
import com._s3k.runsync.domain.run.dto.request.RunSessionEndReq;
import com._s3k.runsync.domain.run.dto.request.RunSessionStartReq;
import com._s3k.runsync.domain.run.dto.response.RunSessionEndRes;
import com._s3k.runsync.domain.run.dto.response.RunSessionStartRes;
import com._s3k.runsync.domain.run.exception.RunSessionErrorCode;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.run.repository.RunSessionRedisRepository;
import com._s3k.runsync.domain.run.repository.RunningSessionRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.RunPath;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import com._s3k.runsync.global.exception.GlobalException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RunSessionService {

    private static final TypeReference<Map<String, Object>> PATH_TYPE_REF = new TypeReference<>() {};

    private final UserRepository userRepository;
    private final RunningSessionRepository runningSessionRepository;
    private final RunRecordRepository runRecordRepository;
    private final RunSessionRedisRepository runSessionRedisRepository;
    private final LocationService locationService;
    private final ObjectMapper objectMapper;


    @Transactional
    public RunSessionStartRes createRunSession(Long userId, RunSessionStartReq request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        // 일반적인 경우 서비스 레벨에서 차단
        if (runningSessionRepository.existsByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)) {
            throw new GlobalException(RunSessionErrorCode.ACTIVE_SESSION_ALREADY_EXISTS);
        }

        try {
            RunningSession session = RunningSession.of(user, request.getStartTime());
            runningSessionRepository.save(session);
            return RunSessionStartRes.of(session);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청 시 DB 유니크 인덱스(idx_user_active_session)가 잡아주는 케이스
            throw new GlobalException(RunSessionErrorCode.ACTIVE_SESSION_ALREADY_EXISTS);
        }
    }

    public void appendPath(Long sessionId, double latitude, double longitude, double speed) {
        runSessionRedisRepository.appendPath(sessionId, latitude, longitude, speed);
    }

    @Transactional
    public void updateLocation(Long userId, Long sessionId, LocationUpdateReq request) {
        RunningSession session = runningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GlobalException(RunSessionErrorCode.SESSION_NOT_FOUND));

        if (!session.getUserId().equals(userId)) {
            throw new GlobalException(RunSessionErrorCode.SESSION_NOT_OWNER);
        }

        if (session.getStatus() != RunningSessionStatus.ACTIVE) {
            throw new GlobalException(RunSessionErrorCode.SESSION_NOT_ACTIVE);
        }

        session.updateLocation(request.getLastLatitude(), request.getLastLongitude(),
                BigDecimal.valueOf(request.getCurrentDistance()), request.getCurrentDurationTime());
    }

    @Transactional
    public RunSessionEndRes endRunSession(Long userId, Long sessionId, RunSessionEndReq request) {
        RunningSession session = runningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GlobalException(RunSessionErrorCode.SESSION_NOT_FOUND));

        if (!session.getUserId().equals(userId)) {
            throw new GlobalException(RunSessionErrorCode.SESSION_NOT_OWNER);
        }

        if (session.getStatus() != RunningSessionStatus.ACTIVE) {
            throw new GlobalException(RunSessionErrorCode.SESSION_NOT_ACTIVE);
        }

        BigDecimal totalDistance = BigDecimal.valueOf(request.getTotalDistance());
        session.complete(request.getEndTime(), totalDistance);

        RunRecord runRecord = session.createRecord(totalDistance);

        List<String> pathJsonList = runSessionRedisRepository.getPaths(sessionId);
        if (!pathJsonList.isEmpty()) {
            runRecord.addPaths(parsePaths(pathJsonList, runRecord));
        }

        runRecordRepository.save(runRecord);

        runSessionRedisRepository.deletePaths(sessionId);
        runSessionRedisRepository.deleteState(sessionId);
        locationService.removeLocation(userId);

        return RunSessionEndRes.of(session);
    }

    private List<RunPath> parsePaths(List<String> pathJsonList, RunRecord runRecord) {
        List<RunPath> paths = new ArrayList<>();
        for (int i = 0; i < pathJsonList.size(); i++) {
            try {
                Map<String, Object> pathData = objectMapper.readValue(pathJsonList.get(i), PATH_TYPE_REF);
                double lat = ((Number) pathData.get("lat")).doubleValue();
                double lng = ((Number) pathData.get("lng")).doubleValue();
                double speed = ((Number) pathData.get("speed")).doubleValue();
                LocalDateTime recordedAt = LocalDateTime.ofInstant(
                        Instant.parse((String) pathData.get("recordedAt")), ZoneOffset.UTC);
                BigDecimal speedKmh = BigDecimal.valueOf(speed * 3.6).setScale(2, RoundingMode.HALF_UP);

                paths.add(RunPath.of(runRecord, lat, lng, i + 1, recordedAt, null, speedKmh, null, null));
            } catch (Exception e) {
                throw new GlobalException(RunSessionErrorCode.PATH_PARSE_FAILED);
            }
        }
        return paths;
    }
}
