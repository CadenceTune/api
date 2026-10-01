package com.cadencetune.api.domain.playlist.client;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorClient {

  private final RestClient processorRestClient;

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
    } catch (Exception e) {
      // 외부 프로세서 연동 실패가 전체 서비스 중단으로 이어지지 않도록 로그 기록 후 빈 리스트 반환
      log.error("[ProcessorClient] 파이썬 프로세서 통신 에러: {}", e.getMessage(), e);
    }

    return Collections.emptyList();
  }

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
    } catch (Exception e) {
      // BPM 측정 실패 시 기본값(0.0)을 반환하여 서비스 예외 전파 방지
      log.error("[ProcessorClient BPM] 통신 에러: {}", e.getMessage(), e);
    }
    return 0.0;
  }
}
