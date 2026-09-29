package com.cadencetune.api.domain.playlist.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cadencetune.api.domain.playlist.dto.request.PlaylistRegisterRequest;
import com.cadencetune.api.domain.playlist.dto.response.PlaylistResponseDto;
import com.cadencetune.api.domain.playlist.service.PlaylistService;
import com.cadencetune.api.global.error.BusinessException;
import com.cadencetune.api.global.error.ErrorCode;
import com.cadencetune.api.global.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlaylistController.class)
@Import(GlobalExceptionHandler.class)
class PlaylistControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private PlaylistService playlistService;

  @Test
  @DisplayName("플레이리스트 등록 성공 시 ApiResponse SUCCESS 규격으로 응답한다")
  void registerPlaylistSuccess() throws Exception {
    PlaylistResponseDto responseDto =
        new PlaylistResponseDto(
            1L, "https://www.youtube.com/playlist?list=sample", Collections.emptyList());
    given(playlistService.registerPlaylist(anyString())).willReturn(responseDto);

    PlaylistRegisterRequest request =
        new PlaylistRegisterRequest("https://www.youtube.com/playlist?list=sample");

    mockMvc
        .perform(
            post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.url").value("https://www.youtube.com/playlist?list=sample"))
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.nullValue()));
  }

  @Test
  @DisplayName("존재하지 않는 플레이리스트 BPM 분석 요청 시 PLAYLIST_NOT_FOUND 에러를 응답한다")
  void analyzePlaylistNotFound() throws Exception {
    willThrow(new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND))
        .given(playlistService)
        .analyzePlaylistBpm(anyLong());

    mockMvc
        .perform(post("/api/playlists/999/analyze"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value("PLAYLIST_NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("존재하지 않는 플레이리스트입니다."));
  }
}
