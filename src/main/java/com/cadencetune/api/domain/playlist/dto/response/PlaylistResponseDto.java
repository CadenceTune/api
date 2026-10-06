package com.cadencetune.api.domain.playlist.dto.response;

import com.cadencetune.api.domain.playlist.entity.YoutubePlaylist;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class PlaylistResponseDto {

  private final Long id;
  private final String url;
  private final List<TrackResponseDto> tracks;

  public PlaylistResponseDto(Long id, String url, List<TrackResponseDto> tracks) {
    this.id = id;
    this.url = url;
    this.tracks = tracks;
  }

  public PlaylistResponseDto(YoutubePlaylist playlist) {
    this.id = playlist.getId();
    this.url = playlist.getUrl();
    this.tracks =
        playlist.getTracks().stream()
            .map(t -> new TrackResponseDto(t.getTrack()))
            .collect(Collectors.toList());
  }
}
