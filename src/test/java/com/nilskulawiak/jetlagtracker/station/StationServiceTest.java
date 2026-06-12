package com.nilskulawiak.jetlagtracker.station;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nilskulawiak.jetlagtracker.action.GameActionService;
import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.game.GameStatus;
import com.nilskulawiak.jetlagtracker.team.Team;
import com.nilskulawiak.jetlagtracker.team.TeamRepository;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock private StationRepository stationRepository;
    @Mock private GameRepository gameRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private StationChipStateRepository stationChipStateRepository;
    @Mock private GameActionService gameActionService;

    @InjectMocks
    private StationService stationService;

    @Test
    void createStationSavesStationAndLogsAction() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.CREATED);
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(stationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StationResponse response = stationService.createStation(
                game.getId(), new CreateStationRequest("Taipei", 10, 20));

        assertThat(response.name()).isEqualTo("Taipei");
        verify(stationRepository).save(any(Station.class));
        verify(gameActionService).log(any(), any(), any());
    }

    @Test
    void createStationThrowsWhenGameAlreadyStarted() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));

        assertThatThrownBy(() -> stationService.createStation(
                game.getId(), new CreateStationRequest("Taipei", 10, 20)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stations can only be created before the game starts");
    }

    @Test
    void addChipsFirstPlacementOnUnownedStation() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        Team team = teamWithId(UUID.randomUUID(), game, 10);
        Station station = stationWithId(UUID.randomUUID(), game);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(stationChipStateRepository.findByStationAndTeam(station, team)).thenReturn(Optional.empty());
        when(stationChipStateRepository.findByStation(station)).thenReturn(List.of());
        when(stationChipStateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StationChipStateResponse response = stationService.addChipsToStation(
                game.getId(), station.getId(), new AddChipsRequest(3, team.getId()));

        assertThat(response.chipsOnStation()).isEqualTo(3);
        assertThat(team.getAvailableChips()).isEqualTo(7);
    }

    @Test
    void addChipsStealingStationFromOpponent() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        Team team = teamWithId(UUID.randomUUID(), game, 20);
        Team opponent = teamWithId(UUID.randomUUID(), game, 0);
        Station station = stationWithId(UUID.randomUUID(), game);
        StationChipState opponentState = chipStateFor(station, opponent, 5);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(stationChipStateRepository.findByStationAndTeam(station, team)).thenReturn(Optional.empty());
        when(stationChipStateRepository.findByStation(station)).thenReturn(List.of(opponentState));
        when(stationChipStateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StationChipStateResponse response = stationService.addChipsToStation(
                game.getId(), station.getId(), new AddChipsRequest(7, team.getId()));

        assertThat(response.chipsOnStation()).isEqualTo(7);
        assertThat(team.getAvailableChips()).isEqualTo(13);
    }

    @Test
    void addChipsThrowsWhenTeamHasInsufficientChips() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        Team team = teamWithId(UUID.randomUUID(), game, 2);
        Station station = stationWithId(UUID.randomUUID(), game);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> stationService.addChipsToStation(
                game.getId(), station.getId(), new AddChipsRequest(5, team.getId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not have enough chips");
    }

    @Test
    void addChipsThrowsWhenExceedingOpponentByMoreThanFive() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        Team team = teamWithId(UUID.randomUUID(), game, 20);
        Team opponent = teamWithId(UUID.randomUUID(), game, 0);
        Station station = stationWithId(UUID.randomUUID(), game);
        StationChipState opponentState = chipStateFor(station, opponent, 3);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(stationChipStateRepository.findByStationAndTeam(station, team)).thenReturn(Optional.empty());
        when(stationChipStateRepository.findByStation(station)).thenReturn(List.of(opponentState));

        assertThatThrownBy(() -> stationService.addChipsToStation(
                game.getId(), station.getId(), new AddChipsRequest(9, team.getId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot exceed opponent chips by more than 5");
    }

    @Test
    void addChipsThrowsWhenNotExceedingOpponentByAtLeastOne() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        Team team = teamWithId(UUID.randomUUID(), game, 20);
        Team opponent = teamWithId(UUID.randomUUID(), game, 0);
        Station station = stationWithId(UUID.randomUUID(), game);
        StationChipState opponentState = chipStateFor(station, opponent, 5);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(stationChipStateRepository.findByStationAndTeam(station, team)).thenReturn(Optional.empty());
        when(stationChipStateRepository.findByStation(station)).thenReturn(List.of(opponentState));

        assertThatThrownBy(() -> stationService.addChipsToStation(
                game.getId(), station.getId(), new AddChipsRequest(4, team.getId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Need to exceed maximum opponent chips by at least 1");
    }

    @Test
    void addChipsThrowsWhenGameNotStarted() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.CREATED);
        Team team = teamWithId(UUID.randomUUID(), game, 10);
        Station station = stationWithId(UUID.randomUUID(), game);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> stationService.addChipsToStation(
                game.getId(), station.getId(), new AddChipsRequest(3, team.getId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Game has not yet started");
    }

    @Test
    void addChipsThrowsWhenStationDoesNotBelongToGame() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        Game otherGame = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        Team team = teamWithId(UUID.randomUUID(), game, 10);
        Station station = stationWithId(UUID.randomUUID(), otherGame);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> stationService.addChipsToStation(
                game.getId(), station.getId(), new AddChipsRequest(3, team.getId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Station does not belong to this game");
    }

    @Test
    void addChipsThrowsWhenTeamDoesNotBelongToGame() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        Game otherGame = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        Team team = teamWithId(UUID.randomUUID(), otherGame, 10);
        Station station = stationWithId(UUID.randomUUID(), game);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> stationService.addChipsToStation(
                game.getId(), station.getId(), new AddChipsRequest(3, team.getId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Team does not belong to this game");
    }

    private static Game gameWithId(UUID id, GameStatus status) {
        Game game = new Game();
        game.setId(id);
        game.setName("Taiwan");
        game.setMapWidth(1000);
        game.setMapHeight(800);
        game.setMapImage("map.png");
        game.setStatus(status);
        return game;
    }

    private static Team teamWithId(UUID id, Game game, int availableChips) {
        Team team = new Team();
        team.setId(id);
        team.setGame(game);
        team.setName("Team " + id);
        team.setColor("#ff0000");
        team.setAvailableChips(availableChips);
        return team;
    }

    private static Station stationWithId(UUID id, Game game) {
        Station station = new Station();
        station.setId(id);
        station.setGame(game);
        station.setName("Station " + id);
        station.setXCoordinate(10);
        station.setYCoordinate(20);
        return station;
    }

    private static StationChipState chipStateFor(Station station, Team team, int chips) {
        StationChipState state = new StationChipState();
        state.setStation(station);
        state.setTeam(team);
        state.setChips(chips);
        return state;
    }
}
