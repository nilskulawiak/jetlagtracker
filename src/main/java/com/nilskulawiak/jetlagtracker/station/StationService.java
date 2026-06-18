package com.nilskulawiak.jetlagtracker.station;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nilskulawiak.jetlagtracker.action.GameActionService;
import com.nilskulawiak.jetlagtracker.action.GameActionType;
import com.nilskulawiak.jetlagtracker.common.exception.NotFoundException;
import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.game.GameStatus;
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
    private final GameActionService gameActionService;

    public StationResponse createStation(UUID gameId, CreateStationRequest request){
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new NotFoundException("Game not found"));

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Stations can only be created before the game starts");
        }

        Station station = new Station();
        station.setName(request.name());
        station.setXCoordinate(request.xCoordinate());
        station.setYCoordinate(request.yCoordinate());
        station.setGame(game);
        stationRepository.save(station);

        gameActionService.log(
                game,
                GameActionType.STATION_CREATED,
                station.getName() + " was created"
        );

        return StationResponse.from(station);
    }
    
    @Transactional
    public StationChipStateResponse addChipsToStation(UUID gameId, UUID stationId, AddChipsRequest request){
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new NotFoundException("Station not found"));
        Team team = teamRepository.findById(request.teamId()).orElseThrow(() -> new NotFoundException("Team not found"));
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NotFoundException("Game not found"));

        if (game.getStatus() != GameStatus.STARTED){
            throw new IllegalArgumentException("Game has not yet started");
        }

        if (!station.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Station does not belong to this game");
        }

        if (!team.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Team does not belong to this game");
        }
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
        int maximumChipsMoreThanOpponent = 5;

        if (resultingChips > maximumOpponentChips + maximumChipsMoreThanOpponent){
            throw new IllegalArgumentException(
                    String.format("Cannot exceed opponent chips by more than %d", maximumChipsMoreThanOpponent)
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

        gameActionService.log(
                game,
                GameActionType.CHIPS_ADDED_TO_STATION,
                team.getName() + " added " + request.chips()
                        + " chips to " + station.getName()
);

        StationChipState savedState = stationChipStateRepository.save(state);
        return StationChipStateResponse.from(savedState);
    }

    @Transactional
    public StationChipStateResponse setStationChips(UUID gameId, UUID stationId, UUID teamId, SetStationChipsRequest request) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("Station not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Team not found"));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        if (game.getStatus() != GameStatus.STARTED) {
            throw new IllegalArgumentException("Game has not yet started");
        }

        if (!station.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Station does not belong to this game");
        }

        if (!team.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Team does not belong to this game");
        }

        StationChipState state = stationChipStateRepository
                .findByStationAndTeam(station, team)
                .orElseGet(() -> {
                    StationChipState newState = new StationChipState();
                    newState.setStation(station);
                    newState.setTeam(team);
                    newState.setChips(0);
                    return newState;
                });

        int delta = request.chips() - state.getChips();
        team.setAvailableChips(team.getAvailableChips() - delta);

        gameActionService.log(
                game,
                GameActionType.CHIPS_CORRECTED,
                team.getName() + " chips at " + station.getName() + " corrected to " + request.chips()
        );

        if (request.chips() == 0) {
            if (state.getId() != null) {
                stationChipStateRepository.delete(state);
            }
            state.setChips(0);
            return StationChipStateResponse.from(state);
        }

        state.setChips(request.chips());
        return StationChipStateResponse.from(stationChipStateRepository.save(state));
    }

    public void deleteStation(UUID gameId, UUID stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("Station not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        if (!station.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Station does not belong to this game");
        }

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Stations can only be deleted before the game starts");
        }

        stationChipStateRepository.deleteByStation(station);
        stationRepository.delete(station);
    }

    public StationResponse patchStation(UUID gameId, UUID stationId, PatchStationRequest request) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("Station not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        if (!station.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Station does not belong to this game");
        }

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Stations can only be updated before the game starts");
        }

        if (request.name() != null) station.setName(request.name());
        if (request.xCoordinate() != null) station.setXCoordinate(request.xCoordinate());
        if (request.yCoordinate() != null) station.setYCoordinate(request.yCoordinate());

        return StationResponse.from(stationRepository.save(station));
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
