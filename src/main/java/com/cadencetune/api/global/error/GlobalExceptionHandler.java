package com.cadencetune.api.global.error;

import com.cadencetune.api.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleCustomException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn(
        "[CustomException] code: {}, message: {}", errorCode.getCode(), errorCode.getMessage());
    return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorCode));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    log.warn("[IllegalArgumentException] message: {}", e.getMessage());
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("INVALID_INPUT_VALUE", e.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("[Unhandled Exception] ", e);
    return ResponseEntity.internalServerError()
        .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
  }
}
