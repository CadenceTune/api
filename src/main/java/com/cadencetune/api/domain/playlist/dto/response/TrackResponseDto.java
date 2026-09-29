package com.cadencetune.api.domain.playlist.dto.response;

import com.cadencetune.api.domain.playlist.entity.Track;
import lombok.Getter;

@Getter
public class TrackResponseDto {

  private final Long id;
  private final String youtubeId;
  private final String title;
  private final String artist;
  private final Long duration;
  private final String url;
  private final String thumbnailUrl;
  private final double bpm;

  public TrackResponseDto(Track track) {
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
