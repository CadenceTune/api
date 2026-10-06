package com.cadencetune.api.domain.playlist.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class YoutubePlaylistTest {

  @Test
  @DisplayName("YoutubePlaylist 생성 메서드로 정상 객체를 생성한다")
  void createPlaylist_Success() {
    // given
    String url = "https://youtube.com/playlist?list=sample";

    // when
    YoutubePlaylist playlist = YoutubePlaylist.from(url);

    // then
    assertThat(playlist).isNotNull();
    assertThat(playlist.getUrl()).isEqualTo(url);
  }

  @Test
  @DisplayName("YoutubePlaylist에 Track 추가 시 매핑 엔티티가 추가 순서대로 position을 가진다")
  void addTrack_Success() {
    // given
    YoutubePlaylist playlist = YoutubePlaylist.from("https://youtube.com/playlist?list=sample");
    Track track1 = track("video1");
    Track track2 = track("video2");

    // when
    playlist.addTrack(track1);
    playlist.addTrack(track2);

    // then
    assertThat(playlist.getTracks())
        .extracting(YoutubePlaylistTrack::getTrack, YoutubePlaylistTrack::getPosition)
        .containsExactly(
            tuple(track1, 0),
            tuple(track2, 1));
    assertThat(playlist.getTracks()).allMatch(t -> t.getYoutubePlaylist() == playlist);
  }

  @Test
  @DisplayName("이미 담긴 Track을 다시 추가하면 중복 매핑하지 않는다")
  void addTrack_Duplicate_Ignored() {
    // given
    YoutubePlaylist playlist = YoutubePlaylist.from("https://youtube.com/playlist?list=sample");
    Track track = track("video1");
    playlist.addTrack(track);

    // when
    playlist.addTrack(track);

    // then
    assertThat(playlist.getTracks()).hasSize(1);
  }

  private Track track(String youtubeId) {
    return Track.builder()
        .youtubeId(youtubeId)
        .title("Test Song")
        .url("https://youtube.com/watch?v=" + youtubeId)
        .build();
  }
}
