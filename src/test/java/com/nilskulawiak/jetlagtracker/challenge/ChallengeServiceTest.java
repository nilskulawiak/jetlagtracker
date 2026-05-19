package com.nilskulawiak.jetlagtracker.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nilskulawiak.jetlagtracker.action.GameActionService;
import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.game.GameStatus;
import com.nilskulawiak.jetlagtracker.team.Team;
import com.nilskulawiak.jetlagtracker.team.TeamRepository;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ChallengeAttemptRepository challengeAttemptRepository;

    @Mock
    private GameActionService gameActionService;

    private ChallengeService challengeService;

    @BeforeEach
    void setUp() {
        challengeService = new ChallengeService(
                challengeRepository,
                gameRepository,
                teamRepository,
                challengeAttemptRepository,
                gameActionService);
    }

    @Test
    void completeChallengeRewardsTeamMarksDoneAndMakesReplacementAvailable() {
        Game game = gameWithId(UUID.randomUUID());
        Team team = teamWithId(UUID.randomUUID(), game);
        Challenge challenge = challengeWithId(UUID.randomUUID(), game, ChallengeStatus.AVAILABLE, 40);
        Challenge replacement = challengeWithId(UUID.randomUUID(), game, ChallengeStatus.CREATED, 25);

        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(challengeAttemptRepository.existsByChallengeAndTeam(challenge, team)).thenReturn(false);
        when(challengeRepository.findByGameAndStatus(game, ChallengeStatus.CREATED)).thenReturn(List.of(replacement));
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));

        ChallengeResponse response = challengeService.completeChallenge(
                game.getId(),
                challenge.getId(),
                new FinishChallengeRequest(team.getId(), null));

        assertThat(response.status()).isEqualTo(ChallengeStatus.DONE);
        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.DONE);
        assertThat(replacement.getStatus()).isEqualTo(ChallengeStatus.AVAILABLE);
        assertThat(team.getAvailableChips()).isEqualTo(40);

        ArgumentCaptor<ChallengeAttempt> attemptCaptor = ArgumentCaptor.forClass(ChallengeAttempt.class);
        verify(challengeAttemptRepository).save(attemptCaptor.capture());
        assertThat(attemptCaptor.getValue().getChallenge()).isSameAs(challenge);
        assertThat(attemptCaptor.getValue().getTeam()).isSameAs(team);
        assertThat(attemptCaptor.getValue().isSuccess()).isTrue();
    }

    @Test
    void failChallengeLeavesChallengeAvailableWhenOtherTeamsCanStillAttempt() {
        Game game = gameWithId(UUID.randomUUID());
        Team team = teamWithId(UUID.randomUUID(), game);
        Challenge challenge = challengeWithId(UUID.randomUUID(), game, ChallengeStatus.AVAILABLE, 10);

        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(challengeAttemptRepository.existsByChallengeAndTeam(challenge, team)).thenReturn(false);
        when(teamRepository.countByGame(game)).thenReturn(2L);
        when(challengeAttemptRepository.countByChallengeAndSuccessFalse(challenge)).thenReturn(1L);
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));

        ChallengeResponse response = challengeService.failChallenge(
                game.getId(),
                challenge.getId(),
                new FinishChallengeRequest(team.getId(), null));

        assertThat(response.status()).isEqualTo(ChallengeStatus.AVAILABLE);
        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.AVAILABLE);
        assertThat(challenge.getReward()).isEqualTo(15);
    }

    @Test
    void failChallengeMarksDoneAndMakesReplacementAvailableWhenAllTeamsFailed() {
        Game game = gameWithId(UUID.randomUUID());
        Team team = teamWithId(UUID.randomUUID(), game);
        Challenge challenge = challengeWithId(UUID.randomUUID(), game, ChallengeStatus.AVAILABLE, 11);
        Challenge replacement = challengeWithId(UUID.randomUUID(), game, ChallengeStatus.CREATED, 30);

        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(challengeAttemptRepository.existsByChallengeAndTeam(challenge, team)).thenReturn(false);
        when(teamRepository.countByGame(game)).thenReturn(2L);
        when(challengeAttemptRepository.countByChallengeAndSuccessFalse(challenge)).thenReturn(2L);
        when(challengeRepository.findByGameAndStatus(game, ChallengeStatus.CREATED)).thenReturn(List.of(replacement));
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));

        ChallengeResponse response = challengeService.failChallenge(
                game.getId(),
                challenge.getId(),
                new FinishChallengeRequest(team.getId(), null));

        assertThat(response.status()).isEqualTo(ChallengeStatus.DONE);
        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.DONE);
        assertThat(challenge.getReward()).isEqualTo(17);
        assertThat(replacement.getStatus()).isEqualTo(ChallengeStatus.AVAILABLE);
    }

    @Test
    void completeChallengeRejectsDuplicateAttempt() {
        Game game = gameWithId(UUID.randomUUID());
        Team team = teamWithId(UUID.randomUUID(), game);
        Challenge challenge = challengeWithId(UUID.randomUUID(), game, ChallengeStatus.AVAILABLE, 10);

        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(challengeAttemptRepository.existsByChallengeAndTeam(challenge, team)).thenReturn(true);
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));

        assertThatThrownBy(() -> challengeService.completeChallenge(
                game.getId(),
                challenge.getId(),
                new FinishChallengeRequest(team.getId(), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Team has already attempted this challenge");

        verify(challengeAttemptRepository).existsByChallengeAndTeam(challenge, team);
    }

    private static Game gameWithId(UUID id) {
        Game game = new Game();
        game.setId(id);
        game.setName("Taiwan");
        game.setMapWidth(1000);
        game.setMapHeight(1000);
        game.setMapImage("taiwan.png");
        game.setStatus(GameStatus.STARTED);
        return game;
    }

    private static Team teamWithId(UUID id, Game game) {
        Team team = new Team();
        team.setId(id);
        team.setGame(game);
        team.setName("Team " + id);
        team.setColor("#ff0000");
        team.setAvailableChips(0);
        return team;
    }

    private static Challenge challengeWithId(UUID id, Game game, ChallengeStatus status, int rewardChips) {
        Challenge challenge = new Challenge();
        challenge.setId(id);
        challenge.setGame(game);
        challenge.setName("Challenge " + id);
        challenge.setXCoordinate(1);
        challenge.setYCoordinate(2);
        challenge.setReward(rewardChips);
        challenge.setStatus(status);
        challenge.setDescription("Test challenge");
        challenge.setChallengeType(ChallengeType.CHIPS);
        return challenge;
    }
}
