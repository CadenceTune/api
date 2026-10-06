package com.cadencetune.api.domain.playlist.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "user_playlist_tracks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_playlist_id", "track_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPlaylistTrack {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_playlist_id")
  private UserPlaylist userPlaylist;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "track_id")
  private Track track;

  private int position;

  UserPlaylistTrack(UserPlaylist userPlaylist, Track track, int position) {
    this.userPlaylist = userPlaylist;
    this.track = track;
    this.position = position;
  }

  void updatePosition(int position) {
    this.position = position;
  }
}
