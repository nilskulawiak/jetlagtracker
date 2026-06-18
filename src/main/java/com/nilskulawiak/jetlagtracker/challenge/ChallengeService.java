package com.nilskulawiak.jetlagtracker.challenge;

import java.util.Collections;
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
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final GameActionService gameActionService;
    private final int numberOfChallengesMadeAvailable = 2; 

    public void deleteChallenge(UUID gameId, UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        if (!challenge.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Challenge does not belong to this game");
        }

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Challenges can only be deleted before the game starts");
        }

        challengeAttemptRepository.deleteByChallenge(challenge);
        challengeRepository.delete(challenge);
    }

    public ChallengeResponse patchChallenge(UUID gameId, UUID challengeId, PatchChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        if (!challenge.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Challenge does not belong to this game");
        }

        if (game.getStatus() == GameStatus.STARTED) {
            boolean hasNonRewardFields = request.name() != null || request.description() != null
                    || request.status() != null || request.challengeType() != null
                    || request.xCoordinate() != null || request.yCoordinate() != null;
            if (hasNonRewardFields) {
                throw new IllegalArgumentException("Only reward can be corrected after the game starts");
            }
            if (request.reward() != null) {
                challenge.setReward(request.reward());
                gameActionService.log(
                        game,
                        GameActionType.CHALLENGE_REWARD_CORRECTED,
                        challenge.getName() + " reward corrected to " + request.reward()
                );
            }
            return ChallengeResponse.from(challenge);
        }

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Challenges can only be updated before the game starts");
        }

        if (request.name() != null) challenge.setName(request.name());
        if (request.description() != null) challenge.setDescription(request.description());
        if (request.reward() != null) challenge.setReward(request.reward());
        if (request.xCoordinate() != null) challenge.setXCoordinate(request.xCoordinate());
        if (request.yCoordinate() != null) challenge.setYCoordinate(request.yCoordinate());
        if (request.challengeType() != null) challenge.setChallengeType(request.challengeType());
        if (request.status() != null) challenge.setStatus(request.status());

        return ChallengeResponse.from(challenge);
    }

    public ChallengeResponse createChallenge(UUID gameId, CreateChallengeRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Challenges can only be created before the game starts");
        }

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
    public ChallengeResponse startChallenge(UUID gameId, UUID challengeId, StartChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge not found"));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new NotFoundException("Team not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        validateSameGame(gameId, team, challenge);

        if (game.getStatus() != GameStatus.STARTED) {
            throw new IllegalArgumentException("Game has not yet started");
        }

        if (ChallengeStatus.AVAILABLE != challenge.getStatus()) {
            throw new IllegalArgumentException("Challenge not available");
        }

        if (challengeAttemptRepository.findByChallengeAndTeam(challenge, team).isPresent()) {
            throw new IllegalArgumentException("Team has already started this challenge");
        }

        ChallengeAttempt attempt = new ChallengeAttempt();
        attempt.setChallenge(challenge);
        attempt.setTeam(team);
        attempt.setStatus(ChallengeAttemptStatus.IN_PROGRESS);
        challengeAttemptRepository.save(attempt);

        gameActionService.log(
                game,
                GameActionType.CHALLENGE_STARTED,
                team.getName() + " started " + challenge.getName()
        );

        return ChallengeResponse.from(challenge);
    }

    @Transactional
    public ChallengeResponse completeChallenge(UUID gameId, UUID challengeId, FinishChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge not found"));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new NotFoundException("Team not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        validateSameGame(gameId, team, challenge);

        if (game.getStatus() != GameStatus.STARTED) {
            throw new IllegalArgumentException("Game has not yet started");
        }

        if (ChallengeStatus.AVAILABLE != challenge.getStatus()) {
            throw new IllegalArgumentException("Challenge not available");
        }

        ChallengeAttempt attempt = challengeAttemptRepository.findByChallengeAndTeam(challenge, team)
                .orElseThrow(() -> new IllegalArgumentException("Team has not started this challenge"));

        if (attempt.getStatus() != ChallengeAttemptStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Team has already resolved this challenge");
        }

        attempt.setStatus(ChallengeAttemptStatus.SUCCESS);

        int chipsEarned;
        switch (challenge.getChallengeType()) {
            case CHIPS -> {
                chipsEarned = challenge.getReward();
                team.setAvailableChips(team.getAvailableChips() + chipsEarned);
            }
            case MULTIPLIER -> {
                int before = team.getAvailableChips();
                team.setAvailableChips(before * challenge.getReward() / 100);
                chipsEarned = team.getAvailableChips() - before;
            }
            case STEAL -> {
                chipsEarned = applyStealReward(challenge, team, request.enemyTeamId());
            }
            case CALL_YOUR_SHOT -> {
                attempt.setCallShot(request.callShot());
                chipsEarned = applyCallYourShotReward(challenge, team, request.callShot());
            }
            default -> throw new IllegalStateException("Unknown challenge type: " + challenge.getChallengeType());
        }

        challenge.setStatus(ChallengeStatus.DONE);
        makeRandomChallengesAvailable(challenge.getGame(), numberOfChallengesMadeAvailable);

        gameActionService.log(
                game,
                GameActionType.CHALLENGE_COMPLETED,
                team.getName() + " completed " + challenge.getName()
                        + " and gained " + chipsEarned + " chips"
        );

        return ChallengeResponse.from(challenge);
    }

    @Transactional
    public ChallengeResponse failChallenge(UUID gameId, UUID challengeId, FinishChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge not found"));

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new NotFoundException("Team not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        validateSameGame(gameId, team, challenge);

        if (game.getStatus() != GameStatus.STARTED) {
            throw new IllegalArgumentException("Game has not yet started");
        }

        if (ChallengeStatus.AVAILABLE != challenge.getStatus()) {
            throw new IllegalArgumentException("Challenge not available");
        }

        ChallengeAttempt attempt = challengeAttemptRepository.findByChallengeAndTeam(challenge, team)
                .orElseThrow(() -> new IllegalArgumentException("Team has not started this challenge"));

        if (attempt.getStatus() != ChallengeAttemptStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Team has already resolved this challenge");
        }

        attempt.setStatus(ChallengeAttemptStatus.FAILED);

        double increaseRewardOnFail = 1.5;
        challenge.setReward((int) Math.ceil(challenge.getReward() * increaseRewardOnFail));

        if (allTeamsFailed(challenge)) {
            challenge.setStatus(ChallengeStatus.DONE);
            makeRandomChallengesAvailable(challenge.getGame(), numberOfChallengesMadeAvailable);
        }

        gameActionService.log(
                game,
                GameActionType.CHALLENGE_FAILED,
                team.getName() + " failed " + challenge.getName()
        );

        return ChallengeResponse.from(challenge);
    }

    @Transactional
    public ChallengeResponse revertChallengeToCreated(UUID gameId, UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        if (!challenge.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Challenge does not belong to this game");
        }

        if (game.getStatus() != GameStatus.STARTED) {
            throw new IllegalArgumentException("Game has not yet started");
        }

        if (challenge.getStatus() != ChallengeStatus.AVAILABLE) {
            throw new IllegalArgumentException("Only AVAILABLE challenges can be reverted to created");
        }

        if (challengeAttemptRepository.existsByChallenge(challenge)) {
            throw new IllegalArgumentException("Cannot revert a challenge that has attempts");
        }

        challenge.setStatus(ChallengeStatus.CREATED);

        gameActionService.log(
                game,
                GameActionType.CHALLENGE_REVERTED_TO_CREATED,
                challenge.getName() + " was reverted to created"
        );

        return ChallengeResponse.from(challenge);
    }

    @Transactional
    public void deleteChallengeAttempt(UUID gameId, UUID challengeId, UUID teamId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Team not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        if (!challenge.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Challenge does not belong to this game");
        }

        if (!team.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Team does not belong to this game");
        }

        if (game.getStatus() != GameStatus.STARTED) {
            throw new IllegalArgumentException("Game has not yet started");
        }

        ChallengeAttempt attempt = challengeAttemptRepository.findByChallengeAndTeam(challenge, team)
                .orElseThrow(() -> new NotFoundException("Attempt not found"));

        challengeAttemptRepository.delete(attempt);

        boolean hasSuccessfulAttempt = challengeAttemptRepository
                .countByChallengeAndStatus(challenge, ChallengeAttemptStatus.SUCCESS) > 0;
        if (challenge.getStatus() == ChallengeStatus.DONE && !hasSuccessfulAttempt) {
            challenge.setStatus(ChallengeStatus.AVAILABLE);
        }

        gameActionService.log(
                game,
                GameActionType.CHALLENGE_ATTEMPT_DELETED,
                team.getName() + "'s attempt at " + challenge.getName() + " was deleted"
        );
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
        long failedAttempts = challengeAttemptRepository.countByChallengeAndStatus(challenge, ChallengeAttemptStatus.FAILED);

        return teamsInGame > 0 && failedAttempts >= teamsInGame;
    }

    private void makeRandomChallengesAvailable(Game game, int count) {
        List<Challenge> createdChallenges = challengeRepository.findByGameAndStatus(game, ChallengeStatus.CREATED);

        if (createdChallenges.isEmpty()) {
            return;
        }

        Collections.shuffle(createdChallenges);
        createdChallenges.stream()
            .limit(count)
            .forEach(c -> c.setStatus(ChallengeStatus.AVAILABLE));
    }

    private int applyStealReward(Challenge challenge, Team team, UUID enemyTeamId) {
        if (enemyTeamId == null) {
            throw new IllegalArgumentException("Enemy team is required for steal challenges");
        }

        if (enemyTeamId.equals(team.getId())) {
            throw new IllegalArgumentException("Team cannot steal from itself");
        }

        Team enemyTeam = teamRepository.findById(enemyTeamId)
                .orElseThrow(() -> new NotFoundException("Enemy team not found"));

        if (!enemyTeam.getGame().getId().equals(team.getGame().getId())) {
            throw new IllegalArgumentException("Enemy team does not belong to this game");
        }

        int stolenAmount = Math.min(
                enemyTeam.getAvailableChips() * challenge.getReward() / 100,
                enemyTeam.getAvailableChips()
        );

        enemyTeam.setAvailableChips(enemyTeam.getAvailableChips() - stolenAmount);
        team.setAvailableChips(team.getAvailableChips() + stolenAmount);

        return stolenAmount;
    }

    private int applyCallYourShotReward(Challenge challenge, Team team, Integer callShot) {
        if (callShot == null || callShot <= 0) {
            throw new IllegalArgumentException("callShot must be a positive number for call-your-shot challenges");
        }

        int chipsEarned = callShot * challenge.getReward();
        team.setAvailableChips(team.getAvailableChips() + chipsEarned);
        return chipsEarned;
    }
}
