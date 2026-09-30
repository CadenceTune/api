package com.cadencetune.api.domain.playlist.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.cadencetune.api.domain.playlist.entity.Playlist;
import com.cadencetune.api.domain.playlist.entity.Track;
import com.cadencetune.api.global.config.JpaConfig;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlaylistRepositoryTest {

  @Autowired private PlaylistRepository playlistRepository;

  @Autowired private TestEntityManager em;

  @Nested
  @DisplayName("플레이리스트 저장 및 조회 테스트")
  class SaveAndFindTest {

    @Test
    @DisplayName("플레이리스트 등록 시 ID가 정상 채번되고 조회된다")
    void saveAndFindPlaylist() {
      // given
      Playlist playlist = Playlist.from("https://www.youtube.com/playlist?list=sample");

      // when
      Playlist savedPlaylist = playlistRepository.save(playlist);

      em.flush();
      em.clear();

      // then
      Optional<Playlist> foundPlaylist = playlistRepository.findById(savedPlaylist.getId());
      assertThat(foundPlaylist).isPresent();
      assertThat(foundPlaylist.get().getUrl())
          .isEqualTo("https://www.youtube.com/playlist?list=sample");
    }

    @Test
    @DisplayName("Playlist에 Track을 추가하면 Cascade.ALL에 의해 Track도 함께 영속화된다")
    void savePlaylistWithTracks_CascadeSuccess() {
      // given
      Playlist playlist = Playlist.from("https://www.youtube.com/playlist?list=sample");

      Track track1 =
          Track.builder()
              .youtubeId("video1")
              .title("곡 제목 1")
              .artist("아티스트 1")
              .duration(180L)
              .url("https://youtube.com/watch?v=video1")
              .thumbnailUrl("https://img.youtube.com/vi/video1/default.jpg")
              .bpm(120.0)
              .build();

      Track track2 =
          Track.builder()
              .youtubeId("video2")
              .title("곡 제목 2")
              .artist("아티스트 2")
              .duration(200L)
              .url("https://youtube.com/watch?v=video2")
              .thumbnailUrl("https://img.youtube.com/vi/video2/default.jpg")
              .bpm(130.0)
              .build();

      playlist.addTrack(track1);
      playlist.addTrack(track2);

      // when
      Playlist savedPlaylist = playlistRepository.save(playlist);

      em.flush();
      em.clear();

      // then
      Playlist foundPlaylist = playlistRepository.findById(savedPlaylist.getId()).orElseThrow();

      assertThat(foundPlaylist.getTracks()).hasSize(2);
      assertThat(foundPlaylist.getTracks()).extracting("title").containsExactly("곡 제목 1", "곡 제목 2");
    }
  }
}
