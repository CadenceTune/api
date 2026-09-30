package com.cadencetune.api.global.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.cadencetune.api.global.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler globalExceptionHandler;

  @BeforeEach
  void setUp() {
    globalExceptionHandler = new GlobalExceptionHandler();
  }

  @Nested
  @DisplayName("비즈니스 예외 핸들링 테스트")
  class BusinessExceptionTest {

    @Test
    @DisplayName("BusinessException 발생 시 올바른 ErrorCode와 Status를 반환한다")
    void handleBusinessException_Success() {
      // given
      BusinessException exception = new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND);

      // when
      ResponseEntity<ApiResponse<Void>> response =
          globalExceptionHandler.handleBusinessException(exception);

      // then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

      ApiResponse<Void> body = response.getBody();
      assertThat(body).isNotNull();
      assertThat(body.getStatus()).isEqualTo("ERROR");
      assertThat(body.getCode()).isEqualTo("PLAYLIST_NOT_FOUND");
      assertThat(body.getMessage()).isEqualTo("존재하지 않는 플레이리스트입니다.");
    }
  }
}
