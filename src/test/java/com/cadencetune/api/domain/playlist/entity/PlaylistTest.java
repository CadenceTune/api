package com.cadencetune.api.domain.playlist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.cadencetune.api.global.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlaylistTest {

  @Autowired private TestEntityManager em;

  @Test
  @DisplayName("엔티티 저장 시 BaseEntity의 생성 및 수정 일시가 자동으로 매핑된다")
  void baseEntity_시간_자동매핑_검증() {
    Playlist playlist = Playlist.from("https://youtube.com/playlist?list=sample");

    em.persistAndFlush(playlist);
    em.clear();

    Playlist found = em.find(Playlist.class, playlist.getId());

    assertThat(found).isNotNull();
    assertThat(found.getCreatedAt()).isNotNull();
    assertThat(found.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Playlist에 Track 추가 시 양방향 연관관계가 정상적으로 설정된다")
  void addTrack_양방향_연관관계_검증() {
    Playlist playlist = Playlist.from("https://youtube.com/playlist?list=sample");
    Track track =
        Track.builder()
            .youtubeId("video123")
            .title("Test Song")
            .url("https://youtube.com/watch?v=video123")
            .build();

    playlist.addTrack(track);

    assertThat(playlist.getTracks()).contains(track);
    assertThat(track.getPlaylist()).isEqualTo(playlist);
  }
}
