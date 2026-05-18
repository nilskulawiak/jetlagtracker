package com.nilskulawiak.jetlagtracker.team;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nilskulawiak.jetlagtracker.game.Game;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    long countByGame(Game game);

    List<Team> findByGame(Game game);
}
