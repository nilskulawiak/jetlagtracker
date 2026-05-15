package com.nilskulawiak.jetlagtracker.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import com.nilskulawiak.jetlagtracker.game.Game;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    long countByGame(Game game);
}
