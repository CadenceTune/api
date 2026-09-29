package com.cadencetune.api.domain.playlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.cadencetune.api.domain.playlist.entity.Playlist;
import com.cadencetune.api.domain.playlist.repository.PlaylistRepository;
import com.cadencetune.api.global.error.BusinessException;
import com.cadencetune.api.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

  @InjectMocks private PlaylistService playlistService;

  @Mock private PlaylistRepository playlistRepository;

  @Nested
  @DisplayName("플레이리스트 조회 테스트")
  class FindPlaylistTest {

    @Test
    @DisplayName("존재하는 ID로 조회 시 Playlist 엔티티를 반환한다")
    void findById_Success() {
      // given
      Long playlistId = 1L;
      Playlist playlist = Playlist.from("https://youtube.com/playlist?list=test");
      given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

      // when
      Playlist result = playlistService.findById(playlistId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getUrl()).isEqualTo("https://youtube.com/playlist?list=test");
      verify(playlistRepository).findById(playlistId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 BusinessException 예외가 발생한다")
    void findById_NotFound_ThrowsBusinessException() {
      // given
      Long nonExistentId = 999L;
      given(playlistRepository.findById(nonExistentId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> playlistService.findById(nonExistentId))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining(ErrorCode.PLAYLIST_NOT_FOUND.getMessage());
    }
  }
}
