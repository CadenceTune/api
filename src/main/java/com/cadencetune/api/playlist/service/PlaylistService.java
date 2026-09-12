package com.cadencetune.api.playlist.service;

import com.cadencetune.api.playlist.client.ProcessorClient;
import com.cadencetune.api.playlist.domain.Playlist;
import com.cadencetune.api.playlist.domain.Track;
import com.cadencetune.api.playlist.dto.PlaylistResponseDto;
import com.cadencetune.api.playlist.repository.PlaylistRepository;
import com.cadencetune.api.playlist.repository.TrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PlaylistService {

    private final ProcessorClient processorClient;
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    public PlaylistService(ProcessorClient processorClient, PlaylistRepository playlistRepository, TrackRepository trackRepository) {
        this.processorClient = processorClient;
        this.playlistRepository = playlistRepository;
        this.trackRepository = trackRepository;
    }

    @Transactional
    public PlaylistResponseDto registerPlaylist(String playlistUrl) {
        List<Map<String, Object>> rawTracks = processorClient.fetchPlaylistFromProcessor(playlistUrl);

        System.out.println("[PlaylistService] 수집된 트랙 개수: " + (rawTracks != null ? rawTracks.size() : 0));

        Playlist playlist = new Playlist(playlistUrl);
        Playlist savedPlaylist = playlistRepository.save(playlist);

        if (rawTracks != null && !rawTracks.isEmpty()) {
            for (Map<String, Object> rawTrack : rawTracks) {
                // snake_case와 camelCase 모두 대비한 방어적 필드 추출
                String youtubeId = rawTrack.get("youtube_id") != null ? (String) rawTrack.get("youtube_id") :
                        (rawTrack.get("youtubeId") != null ? (String) rawTrack.get("youtubeId") : "");
                String title = rawTrack.get("title") != null ? (String) rawTrack.get("title") : "Untitled";
                String artist = rawTrack.get("artist") != null ? (String) rawTrack.get("artist") : "";
                Long duration = rawTrack.get("duration") != null ? ((Number) rawTrack.get("duration")).longValue() : 0L;
                String url = rawTrack.get("url") != null ? (String) rawTrack.get("url") : "";
                String thumbnailUrl = rawTrack.get("thumbnail_url") != null ? (String) rawTrack.get("thumbnail_url") :
                        (rawTrack.get("thumbnailUrl") != null ? (String) rawTrack.get("thumbnailUrl") : "");

                Track track = new Track(youtubeId, title, artist, duration, url, thumbnailUrl, 0.0);

                // 외래키(playlist_id) 세팅 및 양방향 연관관계 연결
                track.setPlaylist(savedPlaylist);
                savedPlaylist.addTrack(track);
            }

            // 명시적으로 Track 리스트를 DB에 일괄 저장
            trackRepository.saveAll(savedPlaylist.getTracks());
        }

        // 최신 DB 상태가 반영된 DTO 응답 생성
        return new PlaylistResponseDto(savedPlaylist);
    }

    @Transactional
    public void analyzePlaylistBpm(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));

        for (Track track : playlist.getTracks()) {
            if (isTrackBpmEmpty(track)) {
                try {
                    double bpm = processorClient.fetchBpmFromProcessor(track.getTitle(), track.getArtist(), track.getUrl());
                    if (bpm > 0.0) {
                        track.setBpm(bpm);
                        trackRepository.save(track);
                    }
                } catch (Exception e) {
                    System.err.println("트랙 ID " + track.getId() + " BPM 분석 실패: " + e.getMessage());
                }
            }
        }
    }

    @Transactional
    public void analyzeAllTracksBpm() {
        List<Track> tracks = trackRepository.findAll();

        for (Track track : tracks) {
            if (isTrackBpmEmpty(track)) {
                try {
                    double bpm = processorClient.fetchBpmFromProcessor(track.getTitle(), track.getArtist(), track.getUrl());
                    if (bpm > 0.0) {
                        track.setBpm(bpm);
                        trackRepository.save(track);
                    }
                } catch (Exception e) {
                    System.err.println("트랙 ID " + track.getId() + " BPM 분석 실패: " + e.getMessage());
                }
            }
        }
    }

    private boolean isTrackBpmEmpty(Track track) {
        return track.getBpm() == 0.0;
    }
}