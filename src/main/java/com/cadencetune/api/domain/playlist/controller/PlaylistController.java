package com.cadencetune.api.domain.playlist.controller;

import com.cadencetune.api.domain.playlist.dto.request.PlaylistRegisterRequest;
import com.cadencetune.api.domain.playlist.dto.response.PlaylistResponseDto;
import com.cadencetune.api.domain.playlist.service.PlaylistService;
import com.cadencetune.api.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

  private final PlaylistService playlistService;

  @PostMapping
  public ResponseEntity<ApiResponse<PlaylistResponseDto>> registerPlaylist(
      @RequestBody PlaylistRegisterRequest request) {
    PlaylistResponseDto response = playlistService.registerPlaylist(request.getPlaylistUrl());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/{id}/analyze")
  public ResponseEntity<ApiResponse<String>> analyzePlaylist(@PathVariable Long id) {
    playlistService.analyzePlaylistBpm(id);
    return ResponseEntity.ok(ApiResponse.success("플레이리스트 BPM 분석이 완료되었습니다."));
  }

  @PostMapping("/analyze-all")
  public ResponseEntity<ApiResponse<String>> analyzeAllTracks() {
    playlistService.analyzeAllTracksBpm();
    return ResponseEntity.ok(ApiResponse.success("모든 트랙의 BPM 분석 및 저장 요청이 완료되었습니다."));
  }
}
