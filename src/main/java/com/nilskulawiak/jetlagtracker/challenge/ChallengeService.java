package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final GameRepository gameRepository;

    public ChallengeResponse createChallenge(UUID gameId, CreateChallengeRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow();

        Challenge challenge = new Challenge();
        challenge.setName(request.name());
        challenge.setXCoordinate(request.xCoordinate());
        challenge.setYCoordinate(request.yCoordinate());
        challenge.setRewardChips(request.rewardChips());
        challenge.setStatus(request.status());
        challenge.setGame(game);

        Challenge savedChallenge = challengeRepository.save(challenge);

        return ChallengeResponse.from(savedChallenge);
    }
}
