package com.nilskulawiak.jetlagtracker.game;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse createGame(@Valid @RequestBody CreateGameRequest request) {
        return gameService.createGame(request);
    }

    @PostMapping("{gameId}/start")
    @ResponseStatus(HttpStatus.OK)
    public GameResponse startGame(@PathVariable UUID gameId, @Valid @RequestBody StartGameRequest request) {
        return gameService.startGame(gameId, request);
    }

    @PostMapping("{gameId}/finish")
    @ResponseStatus(HttpStatus.OK)
    public GameResponse finishGame(@PathVariable UUID gameId) {
        return gameService.finishGame(gameId);
    }

    @GetMapping("/{gameId}/state")
    public GameStateResponse getGameState(@PathVariable UUID gameId) {
        return gameService.getGameState(gameId);
    }

    @GetMapping()
    public GamesResponse getGames() {
        return gameService.getGames();
    }

    @DeleteMapping("{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable UUID gameId) {
        gameService.deleteGame(gameId);
    }

    @PatchMapping("{gameId}")
    public GameResponse patchGame(@PathVariable UUID gameId, @Valid @RequestBody PatchGameRequest request) {
        return gameService.patchGame(gameId, request);
    }

    @PostMapping("/from-preset")
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse createGameFromPreset(
            @Valid @RequestBody CreateGameFromPresetRequest request
    ) {
        return gameService.createGameFromPreset(request);
    }
}