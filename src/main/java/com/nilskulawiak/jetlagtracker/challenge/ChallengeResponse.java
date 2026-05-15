package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

public record ChallengeResponse(
        UUID id,
        UUID gameId,
        String name,
        Double xCoordinate,
        Double yCoordinate,
        Integer rewardChips,
        String status) {
    public static ChallengeResponse from(Challenge challenge) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getGame().getId(),
                challenge.getName(),
                challenge.getXCoordinate(),
                challenge.getYCoordinate(),
                challenge.getRewardChips(),
                challenge.getStatus());
    }
}
