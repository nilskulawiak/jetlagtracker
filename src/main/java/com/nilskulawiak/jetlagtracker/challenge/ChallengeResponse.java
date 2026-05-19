package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

public record ChallengeResponse(
        UUID id,
        UUID gameId,
        String name,
        Integer xCoordinate,
        Integer yCoordinate,
        Integer reward,
        ChallengeStatus status,
        String description,
        ChallengeType challengeType) {
    public static ChallengeResponse from(Challenge challenge) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getGame().getId(),
                challenge.getName(),
                challenge.getXCoordinate(),
                challenge.getYCoordinate(),
                challenge.getReward(),
                challenge.getStatus(),
                challenge.getDescription(),
                challenge.getChallengeType());
    }
}
