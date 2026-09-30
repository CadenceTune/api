package com.cadencetune.api.domain.playlist.entity;

import com.cadencetune.api.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "tracks")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
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

  @Setter private double bpm;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "playlist_id")
  private Playlist playlist;
}
