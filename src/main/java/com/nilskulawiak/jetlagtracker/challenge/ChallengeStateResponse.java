package com.nilskulawiak.jetlagtracker.challenge;

import java.util.List;
import java.util.UUID;

public record ChallengeStateResponse(
        UUID id,
        String name,
        Integer xCoordinate,
        Integer yCoordinate,
        Integer reward,
        ChallengeStatus status,
        String description,
        ChallengeType challengeType,
        List<ChallengeAttemptResponse> challengeAttempts
) {

    public static ChallengeStateResponse from(Challenge challenge, List<ChallengeAttempt> attempts) {
        return new ChallengeStateResponse(
                challenge.getId(),
                challenge.getName(),
                challenge.getXCoordinate(),
                challenge.getYCoordinate(),
                challenge.getReward(),
                challenge.getStatus(),
                challenge.getDescription(),
                challenge.getChallengeType(),
                attempts.stream().map(ChallengeAttemptResponse::from).toList());
    }
}
