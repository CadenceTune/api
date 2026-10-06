package com.cadencetune.api.domain.playlist.entity;

import com.cadencetune.api.global.common.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 유튜브에서 수집한 읽기 전용 원본 재생목록. 트랙은 수집 시점에만 추가된다. */
@Entity
@Getter
@Table(name = "youtube_playlists")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubePlaylist extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 500)
  private String url;

  @OrderBy("position")
  @OneToMany(mappedBy = "youtubePlaylist", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<YoutubePlaylistTrack> tracks = new ArrayList<>();

  private YoutubePlaylist(String url) {
    this.url = url;
  }

  public static YoutubePlaylist from(String url) {
    return new YoutubePlaylist(url);
  }

  /** 원본 재생목록에 같은 곡이 여러 번 들어 있어도 유니크 제약을 지키기 위해 중복은 무시한다. */
  public void addTrack(Track track) {
    if (tracks.stream().anyMatch(t -> t.getTrack() == track)) {
      return;
    }
    tracks.add(new YoutubePlaylistTrack(this, track, tracks.size()));
  }
}
