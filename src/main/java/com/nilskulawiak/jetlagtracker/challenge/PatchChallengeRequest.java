package com.nilskulawiak.jetlagtracker.challenge;

public record PatchChallengeRequest(
        String name,
        String description,
        Integer reward,
        Integer xCoordinate,
        Integer yCoordinate,
        ChallengeType challengeType,
        ChallengeStatus status) {
}
