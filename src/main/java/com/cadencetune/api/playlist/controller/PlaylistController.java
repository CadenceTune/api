package com.cadencetune.api.playlist.controller;

import com.cadencetune.api.playlist.dto.PlaylistResponseDto;
import com.cadencetune.api.playlist.service.PlaylistService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

  private final PlaylistService playlistService;

  public PlaylistController(PlaylistService playlistService) {
    this.playlistService = playlistService;
  }

  @PostMapping
  public ResponseEntity<PlaylistResponseDto> registerPlaylist(
      @RequestBody Map<String, String> request) {
    String playlistUrl = request.get("playlistUrl");
    // PlaylistService에서 이미 PlaylistResponseDto를 생성하여 반환하므로 그대로 응답
    PlaylistResponseDto savedPlaylist = playlistService.registerPlaylist(playlistUrl);
    return ResponseEntity.ok(savedPlaylist);
  }

  @PostMapping("/{id}/analyze")
  public ResponseEntity<String> analyzePlaylist(@PathVariable Long id) {
    playlistService.analyzePlaylistBpm(id);
    return ResponseEntity.ok("플레이리스트 BPM 분석이 완료되었습니다.");
  }

  // 모든 트랙 BPM 일괄 분석 요청
  @PostMapping("/analyze-all")
  public ResponseEntity<String> analyzeAllTracks() {
    playlistService.analyzeAllTracksBpm();
    return ResponseEntity.ok("모든 트랙의 BPM 분석 및 저장 요청이 완료되었습니다.");
  }
}
