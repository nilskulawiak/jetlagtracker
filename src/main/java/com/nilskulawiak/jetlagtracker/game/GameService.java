package com.nilskulawiak.jetlagtracker.game;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nilskulawiak.jetlagtracker.challenge.Challenge;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeRepository;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final ChallengeRepository challengeRepository;

    public GameResponse createGame(CreateGameRequest request) {
        Game game = new Game();
        game.setName(request.name());
        game.setMapHeight(request.mapHeight());
        game.setMapWidth(request.mapWidth());
        game.setMapImage(request.mapImage());

        Game savedGame = gameRepository.save(game);

        return GameResponse.from(savedGame);
    }

    @Transactional
    public GameResponse startGame(UUID gameId, StartGameRequest request){
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        List<Challenge> createdChallenges = challengeRepository.findByGameAndStatus(game, ChallengeStatus.CREATED);

        if (createdChallenges.size() < request.numberOfChallenges()) {
            throw new IllegalArgumentException(
                    "Not enough challenges"
        );
    }

        game.setStatus(GameStatus.STARTED);

        Collections.shuffle(createdChallenges);
        createdChallenges.stream().limit(request.numberOfChallenges()).forEach(challenge -> challenge.setStatus(ChallengeStatus.AVAILABLE));
        return GameResponse.from(game);
    }
}