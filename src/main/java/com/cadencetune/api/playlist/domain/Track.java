package com.cadencetune.api.playlist.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "tracks")
public class Track {

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

    @Setter
    private double bpm;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    public Track(String youtubeId, String title, String artist, Long duration, String url, String thumbnailUrl, double bpm) {
        this.youtubeId = youtubeId;
        this.title = title;
        this.artist = artist != null ? artist.replace("- Topic", "").trim() : "";
        this.duration = duration;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.bpm = bpm;
    }
}