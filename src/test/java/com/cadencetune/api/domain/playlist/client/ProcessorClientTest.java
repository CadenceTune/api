package com.cadencetune.api.domain.playlist.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cadencetune.api.global.error.BusinessException;
import com.cadencetune.api.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class ProcessorClientTest {

  private MockWebServer mockWebServer;
  private ProcessorClient processorClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    String baseUrl = mockWebServer.url("/").toString();

    // 타임아웃 테스트를 위해 read timeout을 1초로 설정한다.
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(1000);
    factory.setReadTimeout(1000);

    RestClient restClient =
        RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .build();

    processorClient = new ProcessorClient(restClient);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Nested
  @DisplayName("플레이리스트 페치 테스트")
  class FetchPlaylistTest {

    @Test
    @DisplayName("외부 프로세서에서 플레이리스트 트랙 목록을 성공적으로 가져온다")
    void fetchPlaylist_Success() throws Exception {
      // given
      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("status", "success");
      responseBody.put(
          "tracks", List.of(Map.of("title", "Test Track", "url", "http://test.com")));

      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setHeader("Content-Type", "application/json")
              .setBody(objectMapper.writeValueAsString(responseBody)));

      // when
      List<Map<String, Object>> tracks =
          processorClient.fetchPlaylistFromProcessor("http://youtube.com/playlist");

      // then
      assertThat(tracks).hasSize(1);
      assertThat(tracks.get(0).get("title")).isEqualTo("Test Track");
    }

    @Test
    @DisplayName("외부 프로세서 500 에러 발생 시 PROCESSOR_ERROR 예외를 던진다")
    void fetchPlaylist_ServerError_ThrowsProcessorError() {
      // given
      mockWebServer.enqueue(new MockResponse().setResponseCode(500));

      // when & then
      assertThatThrownBy(
              () -> processorClient.fetchPlaylistFromProcessor("http://youtube.com/playlist"))
          .isInstanceOf(BusinessException.class)
          .satisfies(
              ex -> assertThat(((BusinessException) ex).getErrorCode())
                  .isEqualTo(ErrorCode.PROCESSOR_ERROR));
    }

    @Test
    @DisplayName("외부 프로세서 타임아웃 발생 시 PROCESSOR_ERROR 예외를 던진다")
    void fetchPlaylist_Timeout_ThrowsProcessorError() {
      // given
      // read timeout(1초)보다 긴 3초 지연 응답을 설정하여 타임아웃을 재현한다.
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setBodyDelay(3, TimeUnit.SECONDS)
              .setBody("{}"));

      // when & then
      assertThatThrownBy(
              () -> processorClient.fetchPlaylistFromProcessor("http://youtube.com/playlist"))
          .isInstanceOf(BusinessException.class)
          .satisfies(
              ex -> assertThat(((BusinessException) ex).getErrorCode())
                  .isEqualTo(ErrorCode.PROCESSOR_ERROR));
    }

    @Test
    @DisplayName("프로세서 응답 status가 success가 아닌 경우 빈 목록을 반환한다")
    void fetchPlaylist_NonSuccessStatus_ReturnsEmptyList() throws Exception {
      // given
      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("status", "error");
      responseBody.put("message", "playlist not found");

      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setHeader("Content-Type", "application/json")
              .setBody(objectMapper.writeValueAsString(responseBody)));

      // when
      List<Map<String, Object>> tracks =
          processorClient.fetchPlaylistFromProcessor("http://youtube.com/playlist");

      // then
      assertThat(tracks).isEmpty();
    }
  }

  @Nested
  @DisplayName("BPM 페치 테스트")
  class FetchBpmTest {

    @Test
    @DisplayName("외부 프로세서에서 곡의 BPM을 성공적으로 가져온다")
    void fetchBpm_Success() throws Exception {
      // given
      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("status", "success");
      responseBody.put("bpm", 128.5);

      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setHeader("Content-Type", "application/json")
              .setBody(objectMapper.writeValueAsString(responseBody)));

      // when
      double bpm = processorClient.fetchBpmFromProcessor("Title", "Artist", "http://youtube.com");

      // then
      assertThat(bpm).isEqualTo(128.5);
    }

    @Test
    @DisplayName("외부 프로세서 500 에러 발생 시 PROCESSOR_ERROR 예외를 던진다")
    void fetchBpm_ServerError_ThrowsProcessorError() {
      // given
      mockWebServer.enqueue(new MockResponse().setResponseCode(500));

      // when & then
      assertThatThrownBy(
              () -> processorClient.fetchBpmFromProcessor("Title", "Artist", "http://youtube.com"))
          .isInstanceOf(BusinessException.class)
          .satisfies(
              ex -> assertThat(((BusinessException) ex).getErrorCode())
                  .isEqualTo(ErrorCode.PROCESSOR_ERROR));
    }

    @Test
    @DisplayName("외부 프로세서 타임아웃 발생 시 PROCESSOR_ERROR 예외를 던진다")
    void fetchBpm_Timeout_ThrowsProcessorError() {
      // given
      // read timeout(1초)보다 긴 3초 지연 응답을 설정하여 타임아웃을 재현한다.
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setBodyDelay(3, TimeUnit.SECONDS)
              .setBody("{}"));

      // when & then
      assertThatThrownBy(
              () -> processorClient.fetchBpmFromProcessor("Title", "Artist", "http://youtube.com"))
          .isInstanceOf(BusinessException.class)
          .satisfies(
              ex -> assertThat(((BusinessException) ex).getErrorCode())
                  .isEqualTo(ErrorCode.PROCESSOR_ERROR));
    }

    @Test
    @DisplayName("응답에서 BPM 파싱에 실패하는 경우 0.0을 반환한다")
    void fetchBpm_ParseFailure_ReturnsZero() throws Exception {
      // given
      // BPM 필드가 없는 응답으로 파싱 실패 시나리오를 재현한다.
      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("status", "success");

      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setHeader("Content-Type", "application/json")
              .setBody(objectMapper.writeValueAsString(responseBody)));

      // when
      double bpm = processorClient.fetchBpmFromProcessor("Title", "Artist", "http://youtube.com");

      // then
      assertThat(bpm).isEqualTo(0.0);
    }
  }
}
