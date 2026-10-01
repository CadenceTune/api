package com.cadencetune.api.global.common;

import com.cadencetune.api.global.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 클라이언트가 일관된 JSON 구조로 파싱할 수 있도록 null 필드도 명시적으로 포함하는 공통 API 응답이다.
 *
 * @param <T> 성공 응답 데이터 타입
 */
@Getter
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

  private final String status;
  private final T data;
  private final String code;
  private final String message;

  private ApiResponse(String status, T data, String code, String message) {
    this.status = status;
    this.data = data;
    this.code = code;
    this.message = message;
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>("SUCCESS", data, null, null);
  }

  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>("SUCCESS", data, null, message);
  }

  public static ApiResponse<Void> error(ErrorCode errorCode) {
    return new ApiResponse<>("ERROR", null, errorCode.getCode(), errorCode.getMessage());
  }

  public static ApiResponse<Void> error(String code, String message) {
    return new ApiResponse<>("ERROR", null, code, message);
  }
}
