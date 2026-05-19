package com.nilskulawiak.jetlagtracker.challenge;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nilskulawiak.jetlagtracker.action.GameActionService;
import com.nilskulawiak.jetlagtracker.action.GameActionType;
import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.game.GameStatus;
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
    private final GameActionService gameActionService;

    public ChallengeResponse createChallenge(UUID gameId, CreateChallengeRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow();

        Challenge challenge = new Challenge();
        challenge.setName(request.name());
        challenge.setXCoordinate(request.xCoordinate());
        challenge.setYCoordinate(request.yCoordinate());
        challenge.setReward(request.reward());
        challenge.setStatus(request.status());
        challenge.setDescription(request.description());
        challenge.setChallengeType(request.challengeType());
        challenge.setGame(game);
        

        Challenge savedChallenge = challengeRepository.save(challenge);

        gameActionService.log(
                game,
                GameActionType.CHALLENGE_CREATED,
                savedChallenge.getName() + " was created"
        );

        return ChallengeResponse.from(savedChallenge);
    }

    @Transactional
    public ChallengeResponse completeChallenge(UUID gameId, UUID challengeId, FinishChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
    
        validateSameGame(gameId, team, challenge);

        if (game.getStatus() != GameStatus.STARTED){
            throw new IllegalArgumentException("Game has not yet started");
        }

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

        switch (challenge.getChallengeType()){
            case CHIPS -> {
                team.setAvailableChips(team.getAvailableChips() + challenge.getReward());
            }
            case MULTIPLIER -> {
                team.setAvailableChips(team.getAvailableChips() * challenge.getReward() / 100);
            }
            case STEAL -> {
                UUID enemyTeamId = request.enemyTeamId();
                applyStealReward(challenge, team, enemyTeamId);
            }
        }

        challenge.setStatus(ChallengeStatus.DONE);
        makeRandomChallengeAvailable(challenge.getGame());

        gameActionService.log(
                game,
                GameActionType.CHALLENGE_COMPLETED,
                team.getName() + " completed " + challenge.getName()
                        + " and gained " + challenge.getReward() + " chips"
        );

        return ChallengeResponse.from(challenge);
    }

    @Transactional
    public ChallengeResponse failChallenge(UUID gameId, UUID challengeId, FinishChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
    
        validateSameGame(gameId, team, challenge);

        if (game.getStatus() != GameStatus.STARTED){
            throw new IllegalArgumentException("Game has not yet started");
        }

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

        challenge.setReward((int) Math.ceil(challenge.getReward() * 1.5));

        if (allTeamsFailed(challenge)) {
            challenge.setStatus(ChallengeStatus.DONE);
            makeRandomChallengeAvailable(challenge.getGame());
        }

        gameActionService.log(
                game,
                GameActionType.CHALLENGE_FAILED,
                team.getName() + " completed " + challenge.getName()
                        + " and gained " + challenge.getReward() + " chips"
        );

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

private void applyStealReward(
        Challenge challenge,
        Team team,
        UUID enemyTeamId
) {

    if (enemyTeamId == null) {
        throw new IllegalArgumentException("Enemy team is required for steal challenges");
    }

    if (enemyTeamId.equals(team.getId())) {
        throw new IllegalArgumentException("Team cannot steal from itself");
    }

    Team enemyTeam = teamRepository.findById(enemyTeamId)
            .orElseThrow(() -> new IllegalArgumentException("Enemy team not found"));

    if (!enemyTeam.getGame().getId().equals(team.getGame().getId())) {
        throw new IllegalArgumentException("Enemy team does not belong to this game");
    }

    int stolenPercent = challenge.getReward();
    int stolenAmount = enemyTeam.getAvailableChips() * stolenPercent / 100;

    enemyTeam.setAvailableChips(enemyTeam.getAvailableChips() - stolenAmount);
    team.setAvailableChips(team.getAvailableChips() + stolenAmount);
}
}
