package com.cadencetune.api.playlist.dto;

import com.cadencetune.api.playlist.domain.Track;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TrackResponseDto(
        Long id,
        @JsonProperty("youtube_id") String youtubeId,
        String title,
        String artist,
        Long duration,
        String url,
        @JsonProperty("thumbnail_url") String thumbnailUrl,
        Double bpm
) {
    public static TrackResponseDto from(Track track) {
        return new TrackResponseDto(
                track.getId(),
                track.getYoutubeId(),
                track.getTitle(),
                track.getArtist(),
                track.getDuration(),
                track.getUrl(),
                track.getThumbnailUrl(),
                track.getBpm()
        );
    }
}