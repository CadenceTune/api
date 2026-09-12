package com.cadencetune.api.playlist.dto;

import com.cadencetune.api.playlist.domain.Playlist;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PlaylistResponseDto {
    private Long id;
    private String url;
    private List<TrackResponseDto> tracks;

    public PlaylistResponseDto(Playlist playlist) {
        this.id = playlist.getId();
        this.url = playlist.getUrl();
        this.tracks = playlist.getTracks().stream()
                .map(TrackResponseDto::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class TrackResponseDto {
        private Long id;
        private String youtubeId;
        private String title;
        private String artist;
        private Long duration;
        private String url;
        private String thumbnailUrl;
        private double bpm;

        public TrackResponseDto(com.cadencetune.api.playlist.domain.Track track) {
            this.id = track.getId();
            this.youtubeId = track.getYoutubeId();
            this.title = track.getTitle();
            this.artist = track.getArtist();
            this.duration = track.getDuration();
            this.url = track.getUrl();
            this.thumbnailUrl = track.getThumbnailUrl();
            this.bpm = track.getBpm();
        }
    }
}