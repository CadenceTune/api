package com.cadencetune.api.domain.playlist.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cadencetune.api.domain.playlist.entity.Track;
import com.cadencetune.api.domain.playlist.entity.UserPlaylist;
import com.cadencetune.api.domain.playlist.entity.UserPlaylistTrack;
import com.cadencetune.api.domain.playlist.entity.YoutubePlaylist;
import com.cadencetune.api.support.annotation.RepositoryTest;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@RepositoryTest
class UserPlaylistRepositoryTest {

  @Autowired private UserPlaylistRepository userPlaylistRepository;

  @Autowired private YoutubePlaylistRepository youtubePlaylistRepository;

  @Autowired private TestEntityManager em;

  @Test
  @DisplayName("트랙 순서를 변경해 저장하면 재조회 시 position 순으로 정렬된다")
  void saveWithReorderedTracks() {
    // given
    Track track1 = em.persist(track("video1"));
    Track track2 = em.persist(track("video2"));
    UserPlaylist playlist = UserPlaylist.of(1L, "러닝용", null);
    playlist.addTrack(track1);
    playlist.addTrack(track2);
    playlist.moveTrack(track2, 0);

    // when
    Long id = userPlaylistRepository.save(playlist).getId();
    em.flush();
    em.clear();

    // then
    UserPlaylist found = userPlaylistRepository.findById(id).orElseThrow();
    assertThat(found.getTracks())
        .extracting(t -> t.getTrack().getYoutubeId())
        .containsExactly("video2", "video1");
  }

  @Test
  @DisplayName("하나의 Track이 UserPlaylist와 YoutubePlaylist에 각각 독립된 position으로 매핑된다")
  void sameTrackInDifferentPlaylists() {
    // given
    Track other = em.persist(track("other"));
    Track shared = em.persist(track("shared"));

    YoutubePlaylist youtubePlaylist =
        YoutubePlaylist.from("https://www.youtube.com/playlist?list=sample");
    youtubePlaylist.addTrack(other);
    youtubePlaylist.addTrack(shared);

    UserPlaylist userPlaylist = UserPlaylist.of(1L, "러닝용", null);
    userPlaylist.addTrack(shared);

    // when
    Long youtubeId = youtubePlaylistRepository.save(youtubePlaylist).getId();
    Long userId = userPlaylistRepository.save(userPlaylist).getId();
    em.flush();
    em.clear();

    // then
    YoutubePlaylist foundYoutube = youtubePlaylistRepository.findById(youtubeId).orElseThrow();
    UserPlaylist foundUser = userPlaylistRepository.findById(userId).orElseThrow();

    assertThat(foundYoutube.getTracks().get(1).getTrack().getId()).isEqualTo(shared.getId());
    assertThat(foundYoutube.getTracks().get(1).getPosition()).isEqualTo(1);
    assertThat(foundUser.getTracks().get(0).getTrack().getId()).isEqualTo(shared.getId());
    assertThat(foundUser.getTracks().get(0).getPosition()).isEqualTo(0);
  }

  @Test
  @DisplayName("동일 플레이리스트에 같은 Track을 중복 매핑하면 유니크 제약조건 위반이 발생한다")
  void duplicateTrack_ViolatesUniqueConstraint() {
    // given
    Track track = em.persist(track("video1"));
    UserPlaylist playlist = UserPlaylist.of(1L, "러닝용", null);
    playlist.addTrack(track);
    playlist.addTrack(track);

    // when & then
    assertThatThrownBy(
            () -> {
              userPlaylistRepository.save(playlist);
              em.flush();
            })
        .hasCauseInstanceOf(ConstraintViolationException.class);
  }

  @Test
  @DisplayName("플레이리스트 조회 시 트랙 매핑과 Track은 지연 로딩된다")
  void tracksAreLazyLoaded() {
    // given
    Track track = em.persist(track("video1"));
    UserPlaylist playlist = UserPlaylist.of(1L, "러닝용", null);
    playlist.addTrack(track);
    Long id = userPlaylistRepository.save(playlist).getId();
    em.flush();
    em.clear();

    // when
    UserPlaylist found = userPlaylistRepository.findById(id).orElseThrow();

    // then
    PersistenceUnitUtil util =
        em.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
    assertThat(util.isLoaded(found, "tracks")).isFalse();

    UserPlaylistTrack mapping = found.getTracks().get(0);
    assertThat(util.isLoaded(mapping, "track")).isFalse();
  }

  private Track track(String youtubeId) {
    return Track.builder()
        .youtubeId(youtubeId)
        .title("Test Song")
        .url("https://youtube.com/watch?v=" + youtubeId)
        .build();
  }
}
