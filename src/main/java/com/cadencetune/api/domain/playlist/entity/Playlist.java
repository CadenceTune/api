package com.cadencetune.api.domain.playlist.entity;

import com.cadencetune.api.global.common.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Table(name = "playlists")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Playlist extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 500)
  private String url;

  @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<Track> tracks = new ArrayList<>();

  public static Playlist from(String url) {
    return Playlist.builder().url(url).build();
  }

  public void addTrack(Track track) {
    this.tracks.add(track);
    track.setPlaylist(this);
  }
}
