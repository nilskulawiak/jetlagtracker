package com.nilskulawiak.jetlagtracker.game;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public GameResponse createGame(CreateGameRequest request) {
        Game game = new Game();
        game.setName(request.name());

        Game savedGame = gameRepository.save(game);

        return GameResponse.from(savedGame);
    }
}