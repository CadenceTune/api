package com.cadencetune.api.domain.playlist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrackTest {

  @Test
  @DisplayName("Track 객체를 정상 생성한다")
  void createTrack_Success() {
    // given & when
    Track track =
        Track.builder()
            .youtubeId("video123")
            .title("Test Song")
            .url("https://youtube.com/watch?v=video123")
            .artist("Artist")
            .duration(180L)
            .thumbnailUrl("https://img.youtube.com/vi/video123/default.jpg")
            .bpm(120.0)
            .build();

    // then
    assertThat(track).isNotNull();
    assertThat(track.getYoutubeId()).isEqualTo("video123");
    assertThat(track.getTitle()).isEqualTo("Test Song");
    assertThat(track.getUrl()).isEqualTo("https://youtube.com/watch?v=video123");
    assertThat(track.getArtist()).isEqualTo("Artist");
    assertThat(track.getDuration()).isEqualTo(180L);
    assertThat(track.getThumbnailUrl())
        .isEqualTo("https://img.youtube.com/vi/video123/default.jpg");
    assertThat(track.getBpm()).isEqualTo(120.0);
  }

  @Test
  @DisplayName("BPM 업데이트 메서드로 BPM 값을 수정한다")
  void updateBpm_Success() {
    // given
    Track track =
        Track.builder()
            .youtubeId("video123")
            .title("Test Song")
            .url("https://youtube.com/watch?v=video123")
            .build();

    // when
    track.updateBpm(128.5);

    // then
    assertThat(track.getBpm()).isEqualTo(128.5);
  }

  @Test
  @DisplayName("Playlist 할당 메서드로 Playlist 연관관계를 설정한다")
  void assignPlaylist_Success() {
    // given
    Track track =
        Track.builder()
            .youtubeId("video123")
            .title("Test Song")
            .url("https://youtube.com/watch?v=video123")
            .build();
    Playlist playlist = Playlist.from("https://youtube.com/playlist?list=sample");

    // when
    track.assignPlaylist(playlist);

    // then
    assertThat(track.getPlaylist()).isNotNull();
    assertThat(track.getPlaylist()).isEqualTo(playlist);
  }
}
