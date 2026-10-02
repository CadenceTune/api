package com.cadencetune.api.domain.playlist.repository;

import com.cadencetune.api.domain.playlist.entity.Track;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Long> {
  Optional<Track> findByYoutubeId(String youtubeId);
}
