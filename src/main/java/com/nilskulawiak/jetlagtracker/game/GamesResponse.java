package com.nilskulawiak.jetlagtracker.game;

import java.util.List;

public record GamesResponse(List<GameResponse> gameResponses) {
    public static GamesResponse from(List<Game> games) {
        return new GamesResponse(
            games.stream().map(GameResponse::from).toList() 
        );
    }
}
