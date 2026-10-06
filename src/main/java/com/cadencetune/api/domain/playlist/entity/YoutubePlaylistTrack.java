package com.cadencetune.api.domain.playlist.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "youtube_playlist_tracks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"youtube_playlist_id", "track_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubePlaylistTrack {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "youtube_playlist_id")
  private YoutubePlaylist youtubePlaylist;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "track_id")
  private Track track;

  private int position;

  YoutubePlaylistTrack(YoutubePlaylist youtubePlaylist, Track track, int position) {
    this.youtubePlaylist = youtubePlaylist;
    this.track = track;
    this.position = position;
  }
}
