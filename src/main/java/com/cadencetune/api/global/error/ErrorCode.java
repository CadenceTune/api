package com.cadencetune.api.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력값입니다."),
  PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAYLIST_NOT_FOUND", "존재하지 않는 플레이리스트입니다."),
  TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "TRACK_NOT_FOUND", "존재하지 않는 트랙입니다."),
  PROCESSOR_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR, "PROCESSOR_ERROR", "파이썬 프로세서 모듈 처리 중 에러가 발생했습니다."),
  INTERNAL_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

  ErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
