package com.nilskulawiak.jetlagtracker.station;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nilskulawiak.jetlagtracker.game.Game;

public interface StationRepository extends JpaRepository<Station, UUID>{
    List<Station> findByGame(Game game);
}
