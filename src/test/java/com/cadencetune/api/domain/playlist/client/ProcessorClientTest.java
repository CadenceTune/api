package com.cadencetune.api.domain.playlist.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ProcessorClientTest {

  private ProcessorClient processorClient;
  private MockRestServiceServer mockServer;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8000");
    this.mockServer = MockRestServiceServer.bindTo(builder).build();
    RestClient restClient = builder.build();
    this.processorClient = new ProcessorClient(restClient);
  }

  @Nested
  @DisplayName("플레이리스트 페치 테스트")
  class FetchPlaylistTest {

    @Test
    @DisplayName("외부 프로세서에서 플레이리스트 트랙 목록을 성공적으로 가져온다")
    void fetchPlaylist_Success() throws Exception {
      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("status", "success");
      responseBody.put("tracks", List.of(Map.of("title", "Test Track", "url", "http://test")));

      mockServer
          .expect(requestTo("http://localhost:8000/api/processor/playlist"))
          .andExpect(method(HttpMethod.POST))
          .andRespond(
              withSuccess(
                  objectMapper.writeValueAsString(responseBody), MediaType.APPLICATION_JSON));

      List<Map<String, Object>> tracks =
          processorClient.fetchPlaylistFromProcessor("http://youtube.com/playlist");

      assertThat(tracks).hasSize(1);
      assertThat(tracks.get(0).get("title")).isEqualTo("Test Track");
      mockServer.verify();
    }

    @Test
    @DisplayName("외부 프로세서 통신 에러 발생 시 빈 리스트를 반환한다")
    void fetchPlaylist_ServerException_ReturnsEmptyList() {
      mockServer
          .expect(requestTo("http://localhost:8000/api/processor/playlist"))
          .andExpect(method(HttpMethod.POST))
          .andRespond(withServerError());

      List<Map<String, Object>> tracks =
          processorClient.fetchPlaylistFromProcessor("http://youtube.com/playlist");

      assertThat(tracks).isEmpty();
      mockServer.verify();
    }
  }

  @Nested
  @DisplayName("BPM 페치 테스트")
  class FetchBpmTest {

    @Test
    @DisplayName("외부 프로세서에서 곡의 BPM을 성공적으로 가져온다")
    void fetchBpm_Success() throws Exception {
      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("status", "success");
      responseBody.put("bpm", 128.5);

      mockServer
          .expect(requestTo("http://localhost:8000/api/processor/bpm"))
          .andExpect(method(HttpMethod.POST))
          .andRespond(
              withSuccess(
                  objectMapper.writeValueAsString(responseBody), MediaType.APPLICATION_JSON));

      double bpm = processorClient.fetchBpmFromProcessor("Title", "Artist", "http://youtube.com");

      assertThat(bpm).isEqualTo(128.5);
      mockServer.verify();
    }
  }
}
