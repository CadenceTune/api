package com.cadencetune.api.domain.playlist.service;

import com.cadencetune.api.domain.playlist.client.ProcessorClient;
import com.cadencetune.api.domain.playlist.dto.response.PlaylistResponseDto;
import com.cadencetune.api.domain.playlist.entity.Playlist;
import com.cadencetune.api.domain.playlist.entity.Track;
import com.cadencetune.api.domain.playlist.repository.PlaylistRepository;
import com.cadencetune.api.domain.playlist.repository.TrackRepository;
import com.cadencetune.api.global.error.BusinessException;
import com.cadencetune.api.global.error.ErrorCode;
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

  public Playlist findById(Long id) {
    return playlistRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));
  }

  /**
   * URL을 정규화하여 중복 등록을 방지하고, 신규 플레이리스트에 프로세서가 수집한 트랙을 연결한다.
   *
   * <p>프로세서의 snake_case 및 camelCase 필드를 지원하며 누락된 메타데이터는 기본값으로 대체한다. 수집된 트랙이 없어도 플레이리스트는 저장한다.
   *
   * @param rawPlaylistUrl 사용자가 전달한 플레이리스트 URL
   * @return 동일한 정규화 URL로 등록된 플레이리스트 또는 새로 저장한 플레이리스트
   */
  @Transactional
  public PlaylistResponseDto registerPlaylist(String rawPlaylistUrl) {
    String playlistUrl = cleanPlaylistUrl(rawPlaylistUrl);

    Optional<Playlist> existingPlaylistOpt = playlistRepository.findByUrl(playlistUrl);
    if (existingPlaylistOpt.isPresent()) {
      Playlist existingPlaylist = existingPlaylistOpt.get();
      log.info(
          "[Playlist 중복] 이미 등록된 플레이리스트입니다 - ID: {} | URL: {}",
          existingPlaylist.getId(),
          playlistUrl);
      return new PlaylistResponseDto(existingPlaylist);
    }

    List<Map<String, Object>> rawTracks = processorClient.fetchPlaylistFromProcessor(playlistUrl);

    log.info("[PlaylistService] 수집된 트랙 개수: {}", rawTracks != null ? rawTracks.size() : 0);

    Playlist playlist = Playlist.from(playlistUrl);
    Playlist savedPlaylist = playlistRepository.save(playlist);

    if (rawTracks != null && !rawTracks.isEmpty()) {
      for (Map<String, Object> rawTrack : rawTracks) {
        // 파이썬 프로세서 응답 포맷(snake_case / camelCase) 유연한 대응 및 기본값 보장
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
          track =
              Track.builder()
                  .youtubeId(youtubeId)
                  .title(title)
                  .artist(artist)
                  .duration(duration)
                  .url(url)
                  .thumbnailUrl(thumbnailUrl)
                  .bpm(0.0)
                  .build();
          log.info("[Track 신규] 새로운 트랙 추가 - {} - {}", artist, title);
        }

        track.assignPlaylist(savedPlaylist);
        savedPlaylist.addTrack(track);
      }

      trackRepository.saveAll(savedPlaylist.getTracks());
      log.info("[PlaylistService] 플레이리스트 트랙 저장 완료 (총 {}개)", savedPlaylist.getTracks().size());
    }

    return new PlaylistResponseDto(savedPlaylist);
  }

  /**
   * 외부 분석 비용을 줄이고 기존 BPM을 보존하기 위해 플레이리스트 내 BPM이 0.0인 트랙만 분석한다.
   *
   * <p>양수 결과만 저장하며, 트랙별 분석 또는 저장 중 예외는 기록하고 다음 트랙 처리를 계속한다.
   *
   * @param playlistId 분석할 플레이리스트 ID
   * @throws BusinessException 플레이리스트가 존재하지 않는 경우
   */
  @Transactional
  public void analyzePlaylistBpm(Long playlistId) {
    Playlist playlist =
        playlistRepository
            .findById(playlistId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

    for (Track track : playlist.getTracks()) {
      // 외부 통신 비용 절감 및 기존 BPM 보존을 위해 미측정(0.0) 트랙만 리소스를 소모하여 계산
      if (isTrackBpmEmpty(track)) {
        try {
          log.info(
              "[BPM 분석 시작] 트랙 ID {} | {} - {}", track.getId(), track.getArtist(), track.getTitle());
          double bpm =
              processorClient.fetchBpmFromProcessor(
                  track.getTitle(), track.getArtist(), track.getUrl());
          if (bpm > 0.0) {
            track.updateBpm(bpm);
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
            track.updateBpm(bpm);
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

  /**
   * 동일한 목록이 서로 다른 공유 URL로 중복 등록되지 않도록 list 파라미터를 표준 YouTube 플레이리스트 URL로 변환한다.
   *
   * @param rawUrl 사용자가 전달한 URL
   * @return null 또는 공백 입력이면 빈 문자열, list 파라미터가 있으면 표준 URL, 그 외에는 앞뒤 공백을 제거한 입력
   */
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
