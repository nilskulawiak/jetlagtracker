package com.nilskulawiak.jetlagtracker.action;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nilskulawiak.jetlagtracker.game.Game;

public interface GameActionRepository extends JpaRepository<GameAction, UUID> {
    List<GameAction> findByGameOrderByCreatedAtDesc(Game game);
}