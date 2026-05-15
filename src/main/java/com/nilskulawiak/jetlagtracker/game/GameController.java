package com.nilskulawiak.jetlagtracker.game;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}