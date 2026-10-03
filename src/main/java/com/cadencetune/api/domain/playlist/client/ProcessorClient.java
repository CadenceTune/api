package com.cadencetune.api.domain.playlist.client;

import com.cadencetune.api.global.error.BusinessException;
import com.cadencetune.api.global.error.ErrorCode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorClient {

  private final RestClient processorRestClient;

  /**
   * 외부 프로세서에서 트랙 목록을 가져온다.
   *
   * <p>5xx 서버 에러나 네트워크 장애(타임아웃 등) 발생 시 PROCESSOR_ERROR로 예외를 전파하여 호출자가 적절히 처리하도록 한다.
   * 응답 status가 "success"가 아닌 경우(비즈니스 실패)는 빈 목록으로 처리한다.
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> fetchPlaylistFromProcessor(String playlistUrl) {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("playlist_url", playlistUrl);

    try {
      Map<String, Object> body =
          processorRestClient
              .post()
              .uri("/api/processor/playlist")
              .contentType(MediaType.APPLICATION_JSON)
              .body(requestBody)
              .retrieve()
              .body(new ParameterizedTypeReference<>() {});

      if (body != null && "success".equals(body.get("status"))) {
        List<Map<String, Object>> tracks = (List<Map<String, Object>>) body.get("tracks");
        return tracks != null ? tracks : Collections.emptyList();
      }

      log.warn("[ProcessorClient] 프로세서 응답 status가 success가 아님 - 빈 목록 반환");
      return Collections.emptyList();

    } catch (RestClientException e) {
      // 5xx 서버 에러, 네트워크 장애(타임아웃 포함) 등 복구 불가 통신 오류는 PROCESSOR_ERROR로 전파한다.
      log.error("[ProcessorClient] 파이썬 프로세서 통신 에러: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.PROCESSOR_ERROR);
    }
  }

  /**
   * 외부 프로세서에 BPM 분석을 요청한다.
   *
   * <p>5xx 서버 에러나 네트워크 장애(타임아웃 등) 발생 시 PROCESSOR_ERROR로 예외를 전파한다.
   * 응답에서 BPM을 파싱할 수 없는 경우(비즈니스 실패)는 미측정 상태를 나타내는 0.0을 반환한다.
   */
  public double fetchBpmFromProcessor(String title, String artist, String youtubeUrl) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("title", title);
    requestBody.put("artist", artist);
    requestBody.put("youtube_url", youtubeUrl);

    try {
      Map<String, Object> body =
          processorRestClient
              .post()
              .uri("/api/processor/bpm")
              .contentType(MediaType.APPLICATION_JSON)
              .body(requestBody)
              .retrieve()
              .body(new ParameterizedTypeReference<>() {});

      if (body != null && "success".equals(body.get("status"))) {
        Object bpmObj = body.get("bpm");
        if (bpmObj instanceof Number) {
          return ((Number) bpmObj).doubleValue();
        }
      }

      log.warn("[ProcessorClient] BPM 파싱 실패 또는 status가 success가 아님 - 0.0 반환");
      return 0.0;

    } catch (RestClientException e) {
      // 5xx 서버 에러, 네트워크 장애(타임아웃 포함) 등 복구 불가 통신 오류는 PROCESSOR_ERROR로 전파한다.
      log.error("[ProcessorClient BPM] 통신 에러: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.PROCESSOR_ERROR);
    }
  }
}
