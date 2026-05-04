package com._s3k.runsync.domain.run.service;

import com._s3k.runsync.domain.run.dto.request.RunSessionStartReq;
import com._s3k.runsync.domain.run.dto.response.RunSessionStartRes;
import com._s3k.runsync.domain.run.exception.RunSessionErrorCode;
import com._s3k.runsync.domain.run.repository.RunningSessionRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import com._s3k.runsync.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RunSessionService {

    private final UserRepository userRepository;
    private final RunningSessionRepository runningSessionRepository;

    @Transactional
    public RunSessionStartRes createRunSession(Long userId, RunSessionStartReq request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

        if (runningSessionRepository.existsByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)) {
            throw new GlobalException(RunSessionErrorCode.ACTIVE_SESSION_ALREADY_EXISTS);
        }

        RunningSession session = RunningSession.of(user, request.getStartTime());
        runningSessionRepository.save(session);

        return RunSessionStartRes.of(session);
    }
}
