package com.cadencetune.api.playlist.service;

import com.cadencetune.api.playlist.client.ProcessorClient;
import com.cadencetune.api.playlist.domain.Playlist;
import com.cadencetune.api.playlist.domain.Track;
import com.cadencetune.api.playlist.dto.PlaylistResponseDto;
import com.cadencetune.api.playlist.repository.PlaylistRepository;
import com.cadencetune.api.playlist.repository.TrackRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PlaylistService {

  private final ProcessorClient processorClient;
  private final PlaylistRepository playlistRepository;
  private final TrackRepository trackRepository;

  public PlaylistService(
      ProcessorClient processorClient,
      PlaylistRepository playlistRepository,
      TrackRepository trackRepository) {
    this.processorClient = processorClient;
    this.playlistRepository = playlistRepository;
    this.trackRepository = trackRepository;
  }

  @Transactional
  public PlaylistResponseDto registerPlaylist(String rawPlaylistUrl) {
    // 1. Clean URL 정규화 (전후 공백 및 si 등 불필요 파라미터 제거)
    String playlistUrl = cleanPlaylistUrl(rawPlaylistUrl);

    // 2. 플레이리스트 중복 체크
    Optional<Playlist> existingPlaylistOpt = playlistRepository.findByUrl(playlistUrl);
    if (existingPlaylistOpt.isPresent()) {
      Playlist existingPlaylist = existingPlaylistOpt.get();
      log.info(
          "[Playlist 중복] 이미 등록된 플레이리스트입니다 - ID: {} | URL: {}",
          existingPlaylist.getId(),
          playlistUrl);
      return new PlaylistResponseDto(existingPlaylist);
    }

    // 3. 신규 플레이리스트인 경우 파이썬 프로세서 호출
    List<Map<String, Object>> rawTracks = processorClient.fetchPlaylistFromProcessor(playlistUrl);

    log.info("[PlaylistService] 수집된 트랙 개수: {}", rawTracks != null ? rawTracks.size() : 0);

    Playlist playlist = new Playlist(playlistUrl);
    Playlist savedPlaylist = playlistRepository.save(playlist);

    if (rawTracks != null && !rawTracks.isEmpty()) {
      for (Map<String, Object> rawTrack : rawTracks) {
        String youtubeId =
            rawTrack.get("youtube_id") != null
                ? (String) rawTrack.get("youtube_id")
                : (rawTrack.get("youtubeId") != null ? (String) rawTrack.get("youtubeId") : "");
        String title = rawTrack.get("title") != null ? (String) rawTrack.get("title") : "Untitled";
        String artist = rawTrack.get("artist") != null ? (String) rawTrack.get("artist") : "";
        Long duration =
            rawTrack.get("duration") != null ? ((Number) rawTrack.get("duration")).longValue() : 0L;
        String url = rawTrack.get("url") != null ? (String) rawTrack.get("url") : "";
        String thumbnailUrl =
            rawTrack.get("thumbnail_url") != null
                ? (String) rawTrack.get("thumbnail_url")
                : (rawTrack.get("thumbnailUrl") != null
                    ? (String) rawTrack.get("thumbnailUrl")
                    : "");

        // DB 트랙 중복 체크 (youtubeId 기준)
        Optional<Track> existingTrackOpt = trackRepository.findByYoutubeId(youtubeId);

        Track track;
        if (existingTrackOpt.isPresent()) {
          track = existingTrackOpt.get();
          log.info(
              "[Track 중복] 이미 DB에 존재하는 트랙 재사용 - ID: {} | {} - {} (BPM: {})",
              track.getId(),
              artist,
              title,
              track.getBpm());
        } else {
          track = new Track(youtubeId, title, artist, duration, url, thumbnailUrl, 0.0);
          log.info("[Track 신규] 새로운 트랙 추가 - {} - {}", artist, title);
        }

        track.setPlaylist(savedPlaylist);
        savedPlaylist.addTrack(track);
      }

      trackRepository.saveAll(savedPlaylist.getTracks());
      log.info("[PlaylistService] 플레이리스트 트랙 저장 완료 (총 {}개)", savedPlaylist.getTracks().size());
    }

    return new PlaylistResponseDto(savedPlaylist);
  }

  @Transactional
  public void analyzePlaylistBpm(Long playlistId) {
    Playlist playlist =
        playlistRepository
            .findById(playlistId)
            .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));

    for (Track track : playlist.getTracks()) {
      if (isTrackBpmEmpty(track)) {
        try {
          log.info(
              "[BPM 분석 시작] 트랙 ID {} | {} - {}", track.getId(), track.getArtist(), track.getTitle());
          double bpm =
              processorClient.fetchBpmFromProcessor(
                  track.getTitle(), track.getArtist(), track.getUrl());
          if (bpm > 0.0) {
            track.setBpm(bpm);
            trackRepository.save(track);
            log.info("[BPM 저장 성공] 트랙 ID {} -> {} BPM", track.getId(), bpm);
          } else {
            log.warn("[BPM 저장 스킵] BPM을 찾을 수 없음 (0.0 반환) - 트랙 ID {}", track.getId());
          }
        } catch (Exception e) {
          log.error("트랙 ID {} BPM 분석 실패: {}", track.getId(), e.getMessage(), e);
        }
      } else {
        log.info("[BPM 분석 스킵] 이미 BPM이 존재하는 트랙 - ID {} (BPM: {})", track.getId(), track.getBpm());
      }
    }
  }

  @Transactional
  public void analyzeAllTracksBpm() {
    List<Track> tracks = trackRepository.findAll();

    for (Track track : tracks) {
      if (isTrackBpmEmpty(track)) {
        try {
          log.info(
              "[BPM 분석 시작] 트랙 ID {} | {} - {}", track.getId(), track.getArtist(), track.getTitle());
          double bpm =
              processorClient.fetchBpmFromProcessor(
                  track.getTitle(), track.getArtist(), track.getUrl());
          if (bpm > 0.0) {
            track.setBpm(bpm);
            trackRepository.save(track);
            log.info("[BPM 저장 성공] 트랙 ID {} -> {} BPM", track.getId(), bpm);
          } else {
            log.warn("[BPM 저장 스킵] BPM을 찾을 수 없음 (0.0 반환) - 트랙 ID {}", track.getId());
          }
        } catch (Exception e) {
          log.error("트랙 ID {} BPM 분석 실패: {}", track.getId(), e.getMessage(), e);
        }
      } else {
        log.info("[BPM 분석 스킵] 이미 BPM이 존재하는 트랙 - ID {} (BPM: {})", track.getId(), track.getBpm());
      }
    }
  }

  private boolean isTrackBpmEmpty(Track track) {
    return track.getBpm() == 0.0;
  }

  private String cleanPlaylistUrl(String rawUrl) {
    if (rawUrl == null || rawUrl.isBlank()) {
      return "";
    }

    String trimmedUrl = rawUrl.trim();

    if (trimmedUrl.contains("list=")) {
      String listId = trimmedUrl.substring(trimmedUrl.indexOf("list=") + 5);
      if (listId.contains("&")) {
        listId = listId.substring(0, listId.indexOf("&"));
      }
      return "https://www.youtube.com/playlist?list=" + listId;
    }

    return trimmedUrl;
  }
}
