package com.nilskulawiak.jetlagtracker.membership;

import com.nilskulawiak.jetlagtracker.game.GameResponse;

import java.util.UUID;

public record GameMembershipResponse(
        UUID gameId,
        MemberRole role,
        UUID teamId,
        String teamName,
        GameResponse game
) {}
