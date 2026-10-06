package com.cadencetune.api.domain.playlist.entity;

import com.cadencetune.api.global.common.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자가 직접 트랙을 추가, 삭제, 순서 변경하는 커스텀 재생목록. */
@Entity
@Getter
@Table(name = "user_playlists")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPlaylist extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String name;

  @Column(length = 1000)
  private String description;

  @OrderBy("position")
  @OneToMany(mappedBy = "userPlaylist", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<UserPlaylistTrack> tracks = new ArrayList<>();

  private UserPlaylist(Long userId, String name, String description) {
    this.userId = userId;
    this.name = name;
    this.description = description;
  }

  public static UserPlaylist of(Long userId, String name, String description) {
    return new UserPlaylist(userId, name, description);
  }

  public void addTrack(Track track) {
    tracks.add(new UserPlaylistTrack(this, track, tracks.size()));
  }

  public void removeTrack(Track track) {
    tracks.removeIf(t -> t.getTrack() == track);
    reindex();
  }

  public void moveTrack(Track track, int newPosition) {
    UserPlaylistTrack target =
        tracks.stream().filter(t -> t.getTrack() == track).findFirst().orElseThrow();
    tracks.remove(target);
    tracks.add(newPosition, target);
    reindex();
  }

  private void reindex() {
    for (int i = 0; i < tracks.size(); i++) {
      tracks.get(i).updatePosition(i);
    }
  }
}
