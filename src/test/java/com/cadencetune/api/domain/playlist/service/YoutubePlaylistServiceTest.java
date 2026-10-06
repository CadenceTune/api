package com.cadencetune.api.domain.playlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.cadencetune.api.domain.playlist.client.ProcessorClient;
import com.cadencetune.api.domain.playlist.dto.response.PlaylistResponseDto;
import com.cadencetune.api.domain.playlist.dto.response.TrackResponseDto;
import com.cadencetune.api.domain.playlist.entity.Track;
import com.cadencetune.api.domain.playlist.entity.YoutubePlaylist;
import com.cadencetune.api.domain.playlist.repository.TrackRepository;
import com.cadencetune.api.domain.playlist.repository.YoutubePlaylistRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YoutubePlaylistServiceTest {

  @InjectMocks private YoutubePlaylistService youtubePlaylistService;

  @Mock private YoutubePlaylistRepository youtubePlaylistRepository;

  @Mock private TrackRepository trackRepository;

  @Mock private ProcessorClient processorClient;

  @Nested
  @DisplayName("플레이리스트 등록 테스트")
  class RegisterPlaylistTest {

    @Test
    @DisplayName("수집된 트랙을 순서대로 매핑하고 재생목록 내 중복 곡은 한 번만 담는다")
    void registerPlaylist_MapsTracksInOrder() {
      // given
      String url = "https://www.youtube.com/playlist?list=sample";
      given(youtubePlaylistRepository.findByUrl(url)).willReturn(Optional.empty());
      given(youtubePlaylistRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
      given(processorClient.fetchPlaylistFromProcessor(url))
          .willReturn(
              List.of(
                  Map.of("youtube_id", "video1", "title", "곡 1"),
                  Map.of("youtube_id", "video2", "title", "곡 2"),
                  Map.of("youtube_id", "video1", "title", "곡 1")));

      Map<String, Track> saved = new HashMap<>();
      given(trackRepository.findByYoutubeId(anyString()))
          .willAnswer(inv -> Optional.ofNullable(saved.get(inv.<String>getArgument(0))));
      given(trackRepository.save(any()))
          .willAnswer(
              inv -> {
                Track track = inv.getArgument(0);
                saved.put(track.getYoutubeId(), track);
                return track;
              });

      // when
      PlaylistResponseDto response = youtubePlaylistService.registerPlaylist(url);

      // then
      assertThat(response.getTracks())
          .extracting(TrackResponseDto::getYoutubeId)
          .containsExactly("video1", "video2");
    }
  }
}
