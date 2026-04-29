package com._s3k.runsync.global.exception;

import com._s3k.runsync.global.common.dto.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // GlobalException 발생 시 반환 형태
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<CommonResponse<Void>> handleException(GlobalException e) {
        log.error("GlobalException occurred: code={}, status={}, message={}",
            e.getResultCode().getCode(),
            e.getResultCode().getStatus(),
            e.getResultCode().getMessage(),
            e
        );
        return ResponseEntity.status(e.getResultCode().getStatus())
            .body(new CommonResponse<>(e.getResultCode()));
    }

    // Validation 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
            .orElse(GlobalErrorCode.VALIDATION_ERROR.getMessage());

        log.warn("Validation failed: {}", message);

        return ResponseEntity
            .status(GlobalErrorCode.VALIDATION_ERROR.getStatus())
            .body(new CommonResponse<>(GlobalErrorCode.VALIDATION_ERROR.getCode(), message));
    }
}
