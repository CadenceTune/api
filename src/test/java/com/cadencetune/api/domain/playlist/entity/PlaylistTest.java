package com.cadencetune.api.domain.playlist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaylistTest {

  @Test
  @DisplayName("Playlist 생성 메서드로 정상 객체를 생성한다")
  void createPlaylist_Success() {
    // given
    String url = "https://youtube.com/playlist?list=sample";

    // when
    Playlist playlist = Playlist.from(url);

    // then
    assertThat(playlist).isNotNull();
    assertThat(playlist.getUrl()).isEqualTo(url);
  }

  @Test
  @DisplayName("Playlist에 Track 추가 시 양방향 연관관계가 정상적으로 설정된다")
  void addTrack_Success() {
    // given
    Playlist playlist = Playlist.from("https://youtube.com/playlist?list=sample");
    Track track =
        Track.builder()
            .youtubeId("video123")
            .title("Test Song")
            .url("https://youtube.com/watch?v=video123")
            .build();

    // when
    playlist.addTrack(track);

    // then
    assertThat(playlist.getTracks()).isNotNull();
    assertThat(playlist.getTracks()).contains(track);
    assertThat(track.getPlaylist()).isEqualTo(playlist);
  }
}
