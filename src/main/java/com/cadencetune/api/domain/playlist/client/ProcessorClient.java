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

  /**
   * 외부 프로세서에서 트랙 목록을 가져오며, 연동 실패가 서비스 예외로 전파되지 않도록 빈 목록으로 대체한다.
   *
   * @param playlistUrl 수집할 플레이리스트 URL
   * @return 수집된 트랙 목록. 응답이 없거나 성공 상태가 아닌 경우, 트랙 목록이 없거나 통신 및 응답 처리에 실패한 경우 빈 목록
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
    } catch (Exception e) {
      // 외부 프로세서 연동 실패가 전체 서비스 중단으로 이어지지 않도록 로그 기록 후 빈 리스트 반환
      log.error("[ProcessorClient] 파이썬 프로세서 통신 에러: {}", e.getMessage(), e);
    }

    return Collections.emptyList();
  }

  /**
   * 외부 프로세서에 BPM 분석을 요청하며, 분석 결과를 얻지 못하면 미측정 상태를 나타내는 0.0을 반환한다.
   *
   * @param title 트랙 제목
   * @param artist 아티스트 이름
   * @param youtubeUrl 분석할 트랙의 YouTube URL
   * @return 성공 응답에 포함된 숫자 BPM. 응답이 없거나 성공 상태가 아닌 경우, 숫자 BPM이 없거나 통신 및 응답 처리에 실패한 경우 0.0
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
    } catch (Exception e) {
      // BPM 측정 실패 시 기본값(0.0)을 반환하여 서비스 예외 전파 방지
      log.error("[ProcessorClient BPM] 통신 에러: {}", e.getMessage(), e);
    }
    return 0.0;
  }
}
