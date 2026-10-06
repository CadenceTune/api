package com.cadencetune.api.domain.playlist.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserPlaylistTest {

  private UserPlaylist playlist;
  private Track track1;
  private Track track2;
  private Track track3;

  @BeforeEach
  void setUp() {
    playlist = UserPlaylist.of(1L, "러닝용", "180 BPM 모음");
    track1 = track("video1");
    track2 = track("video2");
    track3 = track("video3");
    playlist.addTrack(track1);
    playlist.addTrack(track2);
    playlist.addTrack(track3);
  }

  @Test
  @DisplayName("UserPlaylist 생성 메서드로 메타데이터를 가진 객체를 생성한다")
  void createUserPlaylist_Success() {
    assertThat(playlist.getUserId()).isEqualTo(1L);
    assertThat(playlist.getName()).isEqualTo("러닝용");
    assertThat(playlist.getDescription()).isEqualTo("180 BPM 모음");
  }

  @Test
  @DisplayName("Track 추가 시 추가 순서대로 position이 부여된다")
  void addTrack_Success() {
    assertThat(playlist.getTracks())
        .extracting(UserPlaylistTrack::getTrack, UserPlaylistTrack::getPosition)
        .containsExactly(tuple(track1, 0), tuple(track2, 1), tuple(track3, 2));
    assertThat(playlist.getTracks()).allMatch(t -> t.getUserPlaylist() == playlist);
  }

  @Test
  @DisplayName("Track 삭제 시 나머지 트랙의 position이 재정렬된다")
  void removeTrack_Success() {
    // when
    playlist.removeTrack(track1);

    // then
    assertThat(playlist.getTracks())
        .extracting(UserPlaylistTrack::getTrack, UserPlaylistTrack::getPosition)
        .containsExactly(tuple(track2, 0), tuple(track3, 1));
  }

  @Test
  @DisplayName("Track 순서 변경 시 대상 위치로 이동하고 position이 재정렬된다")
  void moveTrack_Success() {
    // when
    playlist.moveTrack(track3, 0);

    // then
    assertThat(playlist.getTracks())
        .extracting(UserPlaylistTrack::getTrack, UserPlaylistTrack::getPosition)
        .containsExactly(tuple(track3, 0), tuple(track1, 1), tuple(track2, 2));
  }

  private Track track(String youtubeId) {
    return Track.builder()
        .youtubeId(youtubeId)
        .title("Test Song")
        .url("https://youtube.com/watch?v=" + youtubeId)
        .build();
  }
}
