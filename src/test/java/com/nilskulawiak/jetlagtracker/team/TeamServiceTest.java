package com.nilskulawiak.jetlagtracker.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nilskulawiak.jetlagtracker.action.GameActionService;
import com.nilskulawiak.jetlagtracker.common.exception.NotFoundException;
import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.game.GameStatus;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock private TeamRepository teamRepository;
    @Mock private GameRepository gameRepository;
    @Mock private GameActionService gameActionService;

    @InjectMocks
    private TeamService teamService;

    @Test
    void createTeamSavesTeamWithExplicitStartingChipsAndLogsAction() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.CREATED);
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(teamRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TeamResponse response = teamService.createTeam(
                game.getId(), new CreateTeamRequest("Runners", "#ff0000", 30));

        assertThat(response.name()).isEqualTo("Runners");
        assertThat(response.availableChips()).isEqualTo(30);
        verify(teamRepository).save(any(Team.class));
        verify(gameActionService).log(any(), any(), any());
    }

    @Test
    void createTeamDefaultsToZeroChipsWhenStartingChipsIsNull() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.CREATED);
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(teamRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TeamResponse response = teamService.createTeam(
                game.getId(), new CreateTeamRequest("Runners", "#ff0000", null));

        assertThat(response.availableChips()).isEqualTo(0);
    }

    @Test
    void createTeamThrowsWhenGameNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.createTeam(id, new CreateTeamRequest("Runners", "#ff0000", 10)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Game not found");
    }

    @Test
    void createTeamThrowsWhenGameAlreadyStarted() {
        Game game = gameWithId(UUID.randomUUID(), GameStatus.STARTED);
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));

        assertThatThrownBy(() -> teamService.createTeam(game.getId(), new CreateTeamRequest("Runners", "#ff0000", 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Teams can only be created before the game starts");
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
}
