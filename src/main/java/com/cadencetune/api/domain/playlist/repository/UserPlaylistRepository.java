package com.cadencetune.api.domain.playlist.repository;

import com.cadencetune.api.domain.playlist.entity.UserPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPlaylistRepository extends JpaRepository<UserPlaylist, Long> {}
