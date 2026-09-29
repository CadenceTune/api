package com.cadencetune.api.playlist.repository;

import com.cadencetune.api.playlist.domain.Playlist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
  Optional<Playlist> findByUrl(String url);
}
