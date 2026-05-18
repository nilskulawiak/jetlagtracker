package com.nilskulawiak.jetlagtracker.preset;

public record ChallengePreset(
        String name,
        String description,
        int rewardChips,
        double xCoordinate,
        double yCoordinate
) {
}
