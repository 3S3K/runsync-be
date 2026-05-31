package com._s3k.runsync.domain.run.service;

import com._s3k.runsync.domain.run.dto.response.RunRecordRes;
import com._s3k.runsync.domain.run.exception.RunRecordErrorCode;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RunRecordService {

    private final RunRecordRepository runRecordRepository;

    @Transactional(readOnly = true)
    public RunRecordRes getRunRecordById(Long userId, Long recordId) {
        RunRecord record = runRecordRepository.findByIdWithPaths(recordId)
                .orElseThrow(() -> new GlobalException(RunRecordErrorCode.RECORD_NOT_FOUND));

        record.validateOwner(userId);

        return RunRecordRes.of(record);
    }
}
