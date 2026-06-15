package com.nilskulawiak.jetlagtracker.game;

import java.util.List;

import com.nilskulawiak.jetlagtracker.action.GameActionResponse;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeStateResponse;
import com.nilskulawiak.jetlagtracker.station.StationStateResponse;
import com.nilskulawiak.jetlagtracker.team.TeamResponse;

public record GameStateResponse(
            GameResponse game,
        List<TeamResponse> teams,
        List<StationStateResponse> stations,
        List<ChallengeStateResponse> challenges,
        List<GameActionResponse> actions
) {
}
