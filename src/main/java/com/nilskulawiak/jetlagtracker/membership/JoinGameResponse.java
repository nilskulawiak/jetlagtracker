package com.nilskulawiak.jetlagtracker.membership;

import com.nilskulawiak.jetlagtracker.game.GameStateResponse;

import java.util.UUID;

public record JoinGameResponse(UUID gameId, MemberRole role, UUID teamId, GameStateResponse game) {}
