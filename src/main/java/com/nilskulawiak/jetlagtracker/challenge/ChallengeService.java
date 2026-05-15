package com.nilskulawiak.jetlagtracker.challenge;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.team.Team;
import com.nilskulawiak.jetlagtracker.team.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;

    public ChallengeResponse createChallenge(UUID gameId, CreateChallengeRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow();

        Challenge challenge = new Challenge();
        challenge.setName(request.name());
        challenge.setXCoordinate(request.xCoordinate());
        challenge.setYCoordinate(request.yCoordinate());
        challenge.setRewardChips(request.rewardChips());
        challenge.setStatus(request.status());
        challenge.setDescription(request.description());
        challenge.setGame(game);

        Challenge savedChallenge = challengeRepository.save(challenge);

        return ChallengeResponse.from(savedChallenge);
    }

    @Transactional
    public ChallengeResponse completeChallenge(UUID gameId, UUID challengeId, FinishChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        validateSameGame(gameId, team, challenge);

        if (ChallengeStatus.AVAILABLE != challenge.getStatus()) {
            throw new IllegalArgumentException("Challenge not available");
        }

        if (challengeAttemptRepository.existsByChallengeAndTeam(challenge, team)) {
            throw new IllegalArgumentException("Team has already attempted this challenge");
        }

        ChallengeAttempt attempt = new ChallengeAttempt();
        attempt.setChallenge(challenge);
        attempt.setTeam(team);
        attempt.setSuccess(true);
        challengeAttemptRepository.save(attempt);

        team.setAvailableChips(team.getAvailableChips() + challenge.getRewardChips());
        challenge.setStatus(ChallengeStatus.DONE);
        makeRandomChallengeAvailable(challenge.getGame());

        return ChallengeResponse.from(challenge);
    }

    @Transactional
    public ChallengeResponse failChallenge(UUID gameId, UUID challengeId, FinishChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        validateSameGame(gameId, team, challenge);

        if (ChallengeStatus.AVAILABLE != challenge.getStatus()) {
            throw new IllegalArgumentException("Challenge not available");
        }

        if (challengeAttemptRepository.existsByChallengeAndTeam(challenge, team)) {
            throw new IllegalArgumentException("Team has already attempted this challenge");
        }

        ChallengeAttempt attempt = new ChallengeAttempt();
        attempt.setChallenge(challenge);
        attempt.setTeam(team);
        attempt.setSuccess(false);
        challengeAttemptRepository.save(attempt);

        challenge.setRewardChips((int) Math.ceil(challenge.getRewardChips() * 1.5));

        if (allTeamsFailed(challenge)) {
            challenge.setStatus(ChallengeStatus.DONE);
            makeRandomChallengeAvailable(challenge.getGame());
        }

        return ChallengeResponse.from(challenge);
    }

    private void validateSameGame(UUID gameId, Team team, Challenge challenge) {
        if (!challenge.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Challenge does not belong to this game");
        }

        if (!team.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Team does not belong to this game");
        }
    }

    private boolean allTeamsFailed(Challenge challenge) {
        long teamsInGame = teamRepository.countByGame(challenge.getGame());
        long failedAttempts = challengeAttemptRepository.countByChallengeAndSuccessFalse(challenge);

        return teamsInGame > 0 && failedAttempts >= teamsInGame;
    }

    private void makeRandomChallengeAvailable(Game game) {
        List<Challenge> createdChallenges = challengeRepository.findByGameAndStatus(game, ChallengeStatus.CREATED);

        if (createdChallenges.isEmpty()) {
            return;
        }

        Challenge nextChallenge = createdChallenges.get(ThreadLocalRandom.current().nextInt(createdChallenges.size()));
        nextChallenge.setStatus(ChallengeStatus.AVAILABLE);
    }
}
