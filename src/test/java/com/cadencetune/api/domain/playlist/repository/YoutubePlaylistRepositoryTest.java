package com.cadencetune.api.domain.playlist.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cadencetune.api.domain.playlist.entity.Track;
import com.cadencetune.api.domain.playlist.entity.YoutubePlaylist;
import com.cadencetune.api.domain.playlist.entity.YoutubePlaylistTrack;
import com.cadencetune.api.support.annotation.RepositoryTest;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@RepositoryTest
class YoutubePlaylistRepositoryTest {

  @Autowired private YoutubePlaylistRepository youtubePlaylistRepository;

  @Autowired private TestEntityManager em;

  @Nested
  @DisplayName("플레이리스트 저장 및 조회 테스트")
  class SaveAndFindTest {

    @Test
    @DisplayName("플레이리스트 등록 시 ID가 정상 채번되고 조회된다")
    void saveAndFindPlaylist() {
      // given
      YoutubePlaylist playlist =
          YoutubePlaylist.from("https://www.youtube.com/playlist?list=sample");

      // when
      YoutubePlaylist savedPlaylist = youtubePlaylistRepository.save(playlist);

      em.flush();
      em.clear();

      // then
      Optional<YoutubePlaylist> foundPlaylist =
          youtubePlaylistRepository.findById(savedPlaylist.getId());
      assertThat(foundPlaylist).isPresent();
      assertThat(foundPlaylist.get().getUrl())
          .isEqualTo("https://www.youtube.com/playlist?list=sample");
    }

    @Test
    @DisplayName("YoutubePlaylist에 Track을 추가하면 Cascade.ALL에 의해 매핑 엔티티가 함께 영속화된다")
    void savePlaylistWithTracks_CascadeSuccess() {
      // given
      YoutubePlaylist playlist =
          YoutubePlaylist.from("https://www.youtube.com/playlist?list=sample");

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

      em.persist(track1);
      em.persist(track2);

      playlist.addTrack(track1);
      playlist.addTrack(track2);

      // when
      YoutubePlaylist savedPlaylist = youtubePlaylistRepository.save(playlist);

      em.flush();
      em.clear();

      // then
      YoutubePlaylist foundPlaylist =
          youtubePlaylistRepository.findById(savedPlaylist.getId()).orElseThrow();

      assertThat(foundPlaylist.getTracks()).hasSize(2);
      assertThat(foundPlaylist.getTracks())
          .extracting("track.title")
          .containsExactly("곡 제목 1", "곡 제목 2");
    }
  }

  @Test
  @DisplayName("플레이리스트 조회 시 트랙 매핑과 Track은 지연 로딩된다")
  void tracksAreLazyLoaded() {
    // given
    YoutubePlaylist playlist = YoutubePlaylist.from("https://www.youtube.com/playlist?list=lazy");
    playlist.addTrack(em.persist(track("video1")));
    Long id = youtubePlaylistRepository.save(playlist).getId();
    em.flush();
    em.clear();

    // when
    YoutubePlaylist found = youtubePlaylistRepository.findById(id).orElseThrow();

    // then
    PersistenceUnitUtil util =
        em.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
    assertThat(util.isLoaded(found, "tracks")).isFalse();

    YoutubePlaylistTrack mapping = found.getTracks().get(0);
    assertThat(util.isLoaded(mapping, "track")).isFalse();
  }

  @Test
  @DisplayName("동일 플레이리스트에 같은 Track을 중복 매핑하면 유니크 제약조건 위반이 발생한다")
  void duplicateTrack_ViolatesUniqueConstraint() {
    // given: addTrack은 중복을 무시하므로 DB 제약은 네이티브 쿼리로 직접 검증한다.
    Long playlistId =
        youtubePlaylistRepository
            .save(YoutubePlaylist.from("https://www.youtube.com/playlist?list=dup"))
            .getId();
    Long trackId = em.persist(track("video1")).getId();
    em.flush();

    // when & then
    assertThatThrownBy(
            () -> {
              for (int position = 0; position < 2; position++) {
                em.getEntityManager()
                    .createNativeQuery(
                        "insert into youtube_playlist_tracks (youtube_playlist_id, track_id, position)"
                            + " values (?1, ?2, ?3)")
                    .setParameter(1, playlistId)
                    .setParameter(2, trackId)
                    .setParameter(3, position)
                    .executeUpdate();
              }
            })
        .isInstanceOf(ConstraintViolationException.class);
  }

  private Track track(String youtubeId) {
    return Track.builder()
        .youtubeId(youtubeId)
        .title("Test Song")
        .url("https://youtube.com/watch?v=" + youtubeId)
        .build();
  }
}
