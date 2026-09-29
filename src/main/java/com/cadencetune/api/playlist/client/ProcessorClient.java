package com.cadencetune.api.playlist.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProcessorClient {

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${processor.url}")
  private String processorUrl;

  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> fetchPlaylistFromProcessor(String playlistUrl) {
    String url = processorUrl + "/api/processor/playlist";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("playlist_url", playlistUrl);

    HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
      Map<String, Object> body = response.getBody();

      System.out.println("[ProcessorClient] 파이썬 프로세서 응답 바디: " + body);

      if (body != null && "success".equals(body.get("status"))) {
        List<Map<String, Object>> tracks = (List<Map<String, Object>>) body.get("tracks");
        System.out.println("[ProcessorClient] 추출된 트랙 수: " + (tracks != null ? tracks.size() : 0));
        return tracks != null ? tracks : Collections.emptyList();
      }
    } catch (Exception e) {
      System.err.println("[ProcessorClient] 파이썬 프로세서 통신 에러: " + e.getMessage());
    }

    return Collections.emptyList();
  }

  public double fetchBpmFromProcessor(String title, String artist, String youtubeUrl) {
    String url = processorUrl + "/api/processor/bpm";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("title", title);
    requestBody.put("artist", artist);
    requestBody.put("youtube_url", youtubeUrl);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
      Map<String, Object> body = response.getBody();

      if (body != null && "success".equals(body.get("status"))) {
        Object bpmObj = body.get("bpm");
        if (bpmObj instanceof Number) {
          return ((Number) bpmObj).doubleValue();
        }
      }
    } catch (Exception e) {
      System.err.println("[ProcessorClient BPM] 통신 에러: " + e.getMessage());
    }
    return 0.0;
  }
}
