package com.nilskulawiak.jetlagtracker.challenge;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nilskulawiak.jetlagtracker.game.Game;

public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {
    List<Challenge> findByGame(Game game);

    List<Challenge> findByGameAndStatus(Game game, ChallengeStatus status);
}
