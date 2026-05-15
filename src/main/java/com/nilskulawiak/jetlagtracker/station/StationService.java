package com.nilskulawiak.jetlagtracker.station;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.team.Team;
import com.nilskulawiak.jetlagtracker.team.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StationService {
    private final StationRepository stationRepository;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final StationChipStateRepository stationChipStateRepository;

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
    
    @Transactional
    public StationChipStateResponse addChipsToStation(UUID gameId, UUID stationId, AddChipsRequest request){
        Station station = stationRepository.findById(stationId).orElseThrow();
        Team team = teamRepository.findById(request.teamId()).orElseThrow();

        if (!station.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Station does not belong to this game");
        }

        if (!team.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Team does not belong to this game");
        }
        request.chips();
        if (team.getAvailableChips() < request.chips()){
            //Later negative route can be implemented
            throw new IllegalArgumentException(String.format("Team %s does not have enough chips", team.getName()));
        }


        StationChipState state =
                stationChipStateRepository
                        .findByStationAndTeam(station, team)
                        .orElseGet(() -> {
                            StationChipState newState = new StationChipState();
                            newState.setStation(station);
                            newState.setTeam(team);
                            newState.setChips(0);
                            return newState;
                        });

        int resultingChips = state.getChips() + request.chips();
        int maximumOpponentChips = getMaximumOpponentChips(station, team);

        if (resultingChips > maximumOpponentChips + 5){
            throw new IllegalArgumentException(
                    "Cannot exceed opponent chips by more than 5"
            );            
        }

        if (resultingChips < maximumOpponentChips + 1){
            throw new IllegalArgumentException(
                    "Need to exceed maximum opponent chips by at least 1"
            );            
        }

        state.setChips(state.getChips() + request.chips());

        team.setAvailableChips(
                team.getAvailableChips() - request.chips()
        );

        StationChipState savedState = stationChipStateRepository.save(state);
        return StationChipStateResponse.from(savedState);
    }

    private int getMaximumOpponentChips(
            Station station,
            Team currentTeam
    ) {

        List<StationChipState> states =
                stationChipStateRepository.findByStation(station);

        return states.stream()
                .filter(state ->
                        !state.getTeam().getId()
                                .equals(currentTeam.getId()))
                .mapToInt(StationChipState::getChips)
                .max()
                .orElse(0);
}
}
