package com.cadencetune.api.domain.playlist.repository;

import com.cadencetune.api.domain.playlist.entity.YoutubePlaylist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YoutubePlaylistRepository extends JpaRepository<YoutubePlaylist, Long> {
  Optional<YoutubePlaylist> findByUrl(String url);
}
