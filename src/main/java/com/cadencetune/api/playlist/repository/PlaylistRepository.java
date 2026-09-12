package com.cadencetune.api.playlist.repository;

import com.cadencetune.api.playlist.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}