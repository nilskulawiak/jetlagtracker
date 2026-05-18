package com.nilskulawiak.jetlagtracker.action;

import org.springframework.stereotype.Service;

import com.nilskulawiak.jetlagtracker.game.Game;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameActionService {

    private final GameActionRepository gameActionRepository;

    public void log(Game game, GameActionType type, String message) {
        GameAction action = new GameAction();
        action.setGame(game);
        action.setType(type);
        action.setMessage(message);

        gameActionRepository.save(action);
    }
}