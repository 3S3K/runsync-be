package com._s3k.runsync.global.exception;

import com._s3k.runsync.global.common.dto.CommonResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    // 처리되지 않은 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleUnexpectedException(Exception e) {
        log.error("Unexpected exception occurred", e);
        return ResponseEntity.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(new CommonResponse<>(GlobalErrorCode.INTERNAL_SERVER_ERROR));
    }

    // @Valid @RequestBody 검증 실패 처리
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

    // @Validated 파라미터(@RequestParam/@PathVariable) 검증 실패 처리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
            .stream()
            .findFirst()
            .map(ConstraintViolation::getMessage)
            .orElse(GlobalErrorCode.VALIDATION_ERROR.getMessage());

        log.warn("Constraint violation: {}", message);

        return ResponseEntity
            .status(GlobalErrorCode.VALIDATION_ERROR.getStatus())
            .body(new CommonResponse<>(GlobalErrorCode.VALIDATION_ERROR.getCode(), message));
    }

    // 요청 파라미터 타입 불일치(잘못된 enum 값 등) 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch for parameter '{}': value={}", e.getName(), e.getValue());

        return ResponseEntity
            .status(GlobalErrorCode.VALIDATION_ERROR.getStatus())
            .body(new CommonResponse<>(GlobalErrorCode.VALIDATION_ERROR));
    }
}
