package com.nilskulawiak.jetlagtracker.game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nilskulawiak.jetlagtracker.action.GameActionRepository;
import com.nilskulawiak.jetlagtracker.action.GameActionService;
import com.nilskulawiak.jetlagtracker.common.exception.NotFoundException;
import com.nilskulawiak.jetlagtracker.challenge.Challenge;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeAttemptRepository;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeRepository;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeStatus;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeType;
import com.nilskulawiak.jetlagtracker.membership.GameInviteRepository;
import com.nilskulawiak.jetlagtracker.membership.GameMembershipRepository;
import com.nilskulawiak.jetlagtracker.preset.ChallengePreset;
import com.nilskulawiak.jetlagtracker.preset.GamePreset;
import com.nilskulawiak.jetlagtracker.preset.GamePresetService;
import com.nilskulawiak.jetlagtracker.preset.StationPreset;
import com.nilskulawiak.jetlagtracker.station.StationChipStateRepository;
import com.nilskulawiak.jetlagtracker.station.StationRepository;
import com.nilskulawiak.jetlagtracker.team.CreateTeamRequest;
import com.nilskulawiak.jetlagtracker.team.TeamRepository;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock private GameRepository gameRepository;
    @Mock private GameMembershipRepository membershipRepository;
    @Mock private GameInviteRepository inviteRepository;
    @Mock private ChallengeRepository challengeRepository;
    @Mock private ChallengeAttemptRepository challengeAttemptRepository;
    @Mock private StationRepository stationRepository;
    @Mock private StationChipStateRepository stationChipStateRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private GameActionService gameActionService;
    @Mock private GameActionRepository gameActionRepository;
    @Mock private GamePresetService gamePresetService;

    @InjectMocks
    private GameService gameService;

    @Test
    void createGameSavesGameAndLogsAction() {
        CreateGameRequest request = new CreateGameRequest("Taiwan", 800, 600, "taiwan.png");
        Game saved = gameWithId(UUID.randomUUID(), "Taiwan");
        when(gameRepository.save(any())).thenReturn(saved);

        GameResponse response = gameService.createGame(request);

        assertThat(response.name()).isEqualTo("Taiwan");
        assertThat(response.status()).isEqualTo(GameStatus.CREATED);
        verify(gameRepository).save(any(Game.class));
        verify(gameActionService).log(any(), any(), any());
    }

    @Test
    void startGameSetsStatusAndMakesExactlyNChallengesAvailable() {
        Game game = gameWithId(UUID.randomUUID(), "Taiwan");
        Challenge c1 = challengeWithId(UUID.randomUUID(), game);
        Challenge c2 = challengeWithId(UUID.randomUUID(), game);
        Challenge c3 = challengeWithId(UUID.randomUUID(), game);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(teamRepository.countByGame(game)).thenReturn(2L);
        when(challengeRepository.findByGameAndStatus(game, ChallengeStatus.CREATED))
                .thenReturn(List.of(c1, c2, c3));

        gameService.startGame(game.getId(), new StartGameRequest(2));

        assertThat(game.getStatus()).isEqualTo(GameStatus.STARTED);

        long available = List.of(c1, c2, c3).stream()
                .filter(c -> c.getStatus() == ChallengeStatus.AVAILABLE)
                .count();
        assertThat(available).isEqualTo(2);
    }

    @Test
    void startGameThrowsWhenNotEnoughChallenges() {
        Game game = gameWithId(UUID.randomUUID(), "Taiwan");

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(teamRepository.countByGame(game)).thenReturn(2L);
        when(challengeRepository.findByGameAndStatus(game, ChallengeStatus.CREATED))
                .thenReturn(List.of(challengeWithId(UUID.randomUUID(), game)));

        assertThatThrownBy(() -> gameService.startGame(game.getId(), new StartGameRequest(3)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not enough challenges");
    }

    @Test
    void startGameThrowsWhenGameNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.startGame(id, new StartGameRequest(1)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Game not found");
    }

    @Test
    void finishGameSetsStatusToDoneAndLogsAction() {
        Game game = gameWithId(UUID.randomUUID(), "Taiwan");
        game.setStatus(GameStatus.STARTED);
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));

        GameResponse response = gameService.finishGame(game.getId());

        assertThat(game.getStatus()).isEqualTo(GameStatus.DONE);
        assertThat(response.status()).isEqualTo(GameStatus.DONE);
        verify(gameActionService).log(any(), any(), any());
    }

    @Test
    void finishGameThrowsWhenGameNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.finishGame(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Game not found");
    }

    @Test
    void createGameFromPresetSavesGameTeamsStationsAndChallenges() {
        GamePreset preset = new GamePreset(
                "taiwan-rail-rush",
                "Taiwan Rail Rush",
                "taiwan.png",
                800, 600,
                List.of(
                        new StationPreset("Taipei", 10, 20),
                        new StationPreset("Taichung", 30, 40)
                ),
                List.of(
                        new ChallengePreset("Sprint", "Run fast", 15, 30, 40, ChallengeType.CHIPS)
                )
        );
        when(gamePresetService.loadPreset("taiwan-rail-rush")).thenReturn(preset);
        when(gameRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateGameFromPresetRequest request = new CreateGameFromPresetRequest(
                "taiwan-rail-rush",
                "My Game",
                List.of(
                        new CreateTeamRequest("Team A", "#ff0000", 30),
                        new CreateTeamRequest("Team B", "#0000ff", 30)
                )
        );

        GameResponse response = gameService.createGameFromPreset(request);

        assertThat(response.name()).isEqualTo("My Game");
        assertThat(response.mapImage()).isEqualTo("taiwan.png");
        verify(teamRepository, times(2)).save(any());
        verify(stationRepository, times(2)).save(any());
        verify(challengeRepository, times(1)).save(any());
    }

    @Test
    void deleteGameRemovesInvitesAndMembershipsBeforeGame() {
        Game game = gameWithId(UUID.randomUUID(), "Taiwan");
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(challengeRepository.findByGame(game)).thenReturn(List.of());
        when(stationRepository.findByGame(game)).thenReturn(List.of());

        gameService.deleteGame(game.getId());

        InOrder order = inOrder(inviteRepository, membershipRepository, gameRepository);
        order.verify(inviteRepository).deleteByGame(game);
        order.verify(membershipRepository).deleteByGame(game);
        order.verify(gameRepository).delete(game);
    }

    @Test
    void deleteGameThrowsWhenGameNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.deleteGame(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Game not found");
    }

    private static Game gameWithId(UUID id, String name) {
        Game game = new Game();
        game.setId(id);
        game.setName(name);
        game.setMapWidth(1000);
        game.setMapHeight(800);
        game.setMapImage("map.png");
        game.setStatus(GameStatus.CREATED);
        return game;
    }

    private static Challenge challengeWithId(UUID id, Game game) {
        Challenge challenge = new Challenge();
        challenge.setId(id);
        challenge.setGame(game);
        challenge.setName("Challenge " + id);
        challenge.setStatus(ChallengeStatus.CREATED);
        challenge.setReward(10);
        return challenge;
    }
}
