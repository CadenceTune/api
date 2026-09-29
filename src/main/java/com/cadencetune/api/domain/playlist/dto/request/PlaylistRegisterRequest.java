package com.cadencetune.api.domain.playlist.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaylistRegisterRequest {

  private String playlistUrl;

  public PlaylistRegisterRequest(String playlistUrl) {
    this.playlistUrl = playlistUrl;
  }
}
