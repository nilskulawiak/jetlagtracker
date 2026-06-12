package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.game.GameStatus;
import com.nilskulawiak.jetlagtracker.team.Team;
import com.nilskulawiak.jetlagtracker.team.TeamRepository;

@SpringBootTest
@Testcontainers
@Transactional
class ChallengeServiceIntegrationTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeAttemptRepository challengeAttemptRepository;

    @Test
    void completeChallengePersistsAttemptRewardsTeamAndOpensReplacement() {
        Game game = gameRepository.save(newGame());
        Team team = teamRepository.save(newTeam(game, "Runners"));
        Challenge active = challengeRepository.save(newChallenge(game, "Active", ChallengeStatus.AVAILABLE, 20));
        Challenge queued = challengeRepository.save(newChallenge(game, "Queued", ChallengeStatus.CREATED, 5));

        challengeService.completeChallenge(game.getId(), active.getId(), new FinishChallengeRequest(team.getId(), null));

        Challenge savedActive = challengeRepository.findById(active.getId()).orElseThrow();
        Challenge savedQueued = challengeRepository.findById(queued.getId()).orElseThrow();
        Team savedTeam = teamRepository.findById(team.getId()).orElseThrow();

        assertThat(savedActive.getStatus()).isEqualTo(ChallengeStatus.DONE);
        assertThat(savedQueued.getStatus()).isEqualTo(ChallengeStatus.AVAILABLE);
        assertThat(savedTeam.getAvailableChips()).isEqualTo(20);
        assertThat(challengeAttemptRepository.existsByChallengeAndTeam(savedActive, savedTeam)).isTrue();
    }

    @Test
    void finalFailedTeamClosesChallengeAndOpensReplacement() {
        Game game = gameRepository.save(newGame());
        Team firstTeam = teamRepository.save(newTeam(game, "First"));
        Team finalTeam = teamRepository.save(newTeam(game, "Final"));
        Challenge active = challengeRepository.save(newChallenge(game, "Active", ChallengeStatus.AVAILABLE, 8));
        Challenge queued = challengeRepository.save(newChallenge(game, "Queued", ChallengeStatus.CREATED, 5));

        challengeService.failChallenge(game.getId(), active.getId(), new FinishChallengeRequest(firstTeam.getId(), null));

        Challenge afterFirstFailure = challengeRepository.findById(active.getId()).orElseThrow();
        assertThat(afterFirstFailure.getStatus()).isEqualTo(ChallengeStatus.AVAILABLE);
        assertThat(afterFirstFailure.getReward()).isEqualTo(12);

        challengeService.failChallenge(game.getId(), active.getId(), new FinishChallengeRequest(finalTeam.getId(), null));

        Challenge afterFinalFailure = challengeRepository.findById(active.getId()).orElseThrow();
        Challenge savedQueued = challengeRepository.findById(queued.getId()).orElseThrow();

        assertThat(afterFinalFailure.getStatus()).isEqualTo(ChallengeStatus.DONE);
        assertThat(afterFinalFailure.getReward()).isEqualTo(18);
        assertThat(savedQueued.getStatus()).isEqualTo(ChallengeStatus.AVAILABLE);
    }

    private static Game newGame() {
        Game game = new Game();
        game.setName("Taiwan " + UUID.randomUUID());
        game.setMapWidth(1000);
        game.setMapHeight(1000);
        game.setMapImage("taiwan.png");
        game.setStatus(GameStatus.STARTED);
        return game;
    }

    private static Team newTeam(Game game, String name) {
        Team team = new Team();
        team.setGame(game);
        team.setName(name);
        team.setColor("#00ff00");
        team.setAvailableChips(0);
        return team;
    }

    private static Challenge newChallenge(Game game, String name, ChallengeStatus status, int rewardChips) {
        Challenge challenge = new Challenge();
        challenge.setGame(game);
        challenge.setName(name);
        challenge.setXCoordinate(10);
        challenge.setYCoordinate(20);
        challenge.setReward(rewardChips);
        challenge.setStatus(status);
        challenge.setDescription("Integration test challenge");
        challenge.setChallengeType(ChallengeType.CHIPS);
        return challenge;
    }
}
