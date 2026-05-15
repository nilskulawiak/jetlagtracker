package com.nilskulawiak.jetlagtracker.station;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StationService {
    private final StationRepository stationRepository;
    private final GameRepository gameRepository;

    public StationResponse createStation(UUID gameId, CreateStationRequest request){
        Game game = gameRepository.findById(gameId).orElseThrow();

        Station station = new Station();
        station.setName(request.name());
        station.setXCoordinate(request.xCoordinate());
        station.setYCoordinate(request.yCoordinate());
        station.setGame(game);
        stationRepository.save(station);
        return StationResponse.from(station);
    }
}
