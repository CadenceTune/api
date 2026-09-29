package com.cadencetune.api.playlist.repository;

import com.cadencetune.api.playlist.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {
    Optional<Track> findByYoutubeId(String youtubeId);
}