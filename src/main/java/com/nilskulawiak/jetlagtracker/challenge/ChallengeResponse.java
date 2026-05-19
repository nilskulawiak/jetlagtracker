package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

public record ChallengeResponse(
        UUID id,
        UUID gameId,
        String name,
        Integer xCoordinate,
        Integer yCoordinate,
        Integer rewardChips,
        ChallengeStatus status,
        String description) {
    public static ChallengeResponse from(Challenge challenge) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getGame().getId(),
                challenge.getName(),
                challenge.getXCoordinate(),
                challenge.getYCoordinate(),
                challenge.getRewardChips(),
                challenge.getStatus(),
                challenge.getDescription());
    }
}
