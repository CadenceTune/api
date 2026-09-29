package com.cadencetune.api.playlist.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "playlists")
public class Playlist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 500)
  private String url;

  @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Track> tracks = new ArrayList<>();

  public Playlist(String url) {
    this.url = url;
  }

  public void addTrack(Track track) {
    tracks.add(track);
    track.setPlaylist(this);
  }
}
