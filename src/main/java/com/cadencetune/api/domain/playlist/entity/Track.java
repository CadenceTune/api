package com.cadencetune.api.domain.playlist.entity;

import com.cadencetune.api.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tracks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Track extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String youtubeId;

  @Column(nullable = false)
  private String title;

  private String artist;

  private Long duration;

  @Column(nullable = false, length = 500)
  private String url;

  @Column(length = 500)
  private String thumbnailUrl;

  private double bpm;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "playlist_id")
  private Playlist playlist;

  @Builder
  public Track(
      String youtubeId,
      String title,
      String url,
      String artist,
      Long duration,
      String thumbnailUrl,
      double bpm) {
    this.youtubeId = youtubeId;
    this.title = title;
    this.url = url;
    this.artist = artist;
    this.duration = duration;
    this.thumbnailUrl = thumbnailUrl;
    this.bpm = bpm;
  }

  public void updateBpm(double bpm) {
    this.bpm = bpm;
  }

  public void assignPlaylist(Playlist playlist) {
    this.playlist = playlist;
  }
}
